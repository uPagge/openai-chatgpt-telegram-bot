package dev.struchkov.example.bot.unit;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.example.bot.util.Cmd;
import dev.struchkov.example.bot.util.UnitName;
import dev.struchkov.godfather.main.domain.annotation.Unit;
import dev.struchkov.godfather.main.domain.content.Attachment;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.simple.domain.unit.AnswerText;
import dev.struchkov.godfather.telegram.domain.ChatAction;
import dev.struchkov.godfather.telegram.domain.ClientBotCommand;
import dev.struchkov.godfather.telegram.domain.attachment.CommandAttachment;
import dev.struchkov.godfather.telegram.main.context.MailPayload;
import dev.struchkov.godfather.telegram.main.core.util.Attachments;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramService;
import dev.struchkov.godfather.telegram.starter.PersonUnitConfiguration;
import dev.struchkov.haiti.utils.Strings;
import dev.struchkov.openai.context.ChatGptService;
import dev.struchkov.openai.context.GPTClient;
import dev.struchkov.openai.domain.chat.ChatInfo;
import dev.struchkov.openai.domain.common.GptMessage;
import dev.struchkov.openai.domain.model.gpt.GPT3Model;
import dev.struchkov.openai.domain.request.GptRequest;
import dev.struchkov.openai.domain.response.Choice;
import dev.struchkov.openai.domain.response.GptResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static dev.struchkov.example.bot.util.UnitName.CLEAR_CONTEXT;
import static dev.struchkov.example.bot.util.UnitName.GPT_UNIT;
import static dev.struchkov.godfather.simple.domain.BoxAnswer.boxAnswer;
import static dev.struchkov.godfather.simple.domain.BoxAnswer.replaceBoxAnswer;

@Component
public class PersonalChatGPTUnit implements PersonUnitConfiguration {

    private ChatInfo chatInfo;

    private final TelegramSending telegramSending;
    private final TelegramService telegramService;
    private final AppProperty appProperty;
    private final GPTClient gptClient;
    private final ChatGptService chatGptService;

    public PersonalChatGPTUnit(
            TelegramSending telegramSending,
            TelegramService telegramService,
            GPTClient gptClient,
            AppProperty appProperty,
            ChatGptService chatGptService
    ) {
        this.telegramSending = telegramSending;
        this.telegramService = telegramService;
        this.appProperty = appProperty;
        this.gptClient = gptClient;
        this.chatGptService = chatGptService;
        this.chatInfo = chatGptService.createChat();
    }

    @PostConstruct
    public void createCommands() {
        telegramService.addCommand(List.of(
                ClientBotCommand.builder()
                        .key(Cmd.CLEAR_CONTEXT)
                        .description("Clears the discussion context. Start a conversation from the beginning")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.HELP)
                        .description("help in use")
                        .build()
        ));
    }

    @Unit(value = UnitName.ACCESS_ERROR, main = true)
    public AnswerText<Mail> accessError() {
        return AnswerText.<Mail>builder()
                .triggerCheck(mail -> !mail.getFromPersonId().equals(appProperty.getTelegramId()))
                .answer(message -> {
                    final StringBuilder messageText = new StringBuilder("\uD83D\uDEA8 *Attempted unauthorized access to the bot*")
                            .append("\n-- -- -- -- --\n");

                    message.getPayLoad(MailPayload.USERNAME)
                            .ifPresent(username -> messageText.append("\uD83E\uDDB9\u200D♂️: @").append(username));

                    messageText.append("\n")
                            .append("\uD83D\uDCAC: ").append(message.getText())
                            .toString();
                    return BoxAnswer.builder()
                            .recipientPersonId(appProperty.getTelegramId())
                            .message(messageText.toString())
                            .build();
                })
                .build();
    }

    @Unit(value = GPT_UNIT, main = true)
    public AnswerText<Mail> chatGpt() {
        return AnswerText.<Mail>builder()
                .triggerCheck(mail -> mail.getFromPersonId().equals(appProperty.getTelegramId()))
                .answer(message -> {
                    telegramService.executeAction(message.getFromPersonId(), ChatAction.TYPING);
                    final BoxAnswer answerWait = BoxAnswer.builder()
                            .recipientPersonId(message.getFromPersonId())
                            .message("Wait... Response is being generated...\nIt might take a long time ⏳")
                            .build();
                    telegramSending.send(answerWait);
                    final String answerText = chatGptService.sendNewMessage(chatInfo.getChatId(), message.getText());
                    return replaceBoxAnswer(answerText);
                })
                .priority(5)
                .build();
    }

    @Unit(value = CLEAR_CONTEXT, main = true)
    public AnswerText<Mail> clearContext() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            if (mail.getFromPersonId().equals(appProperty.getTelegramId())) {
                                final List<Attachment> attachments = mail.getAttachments();
                                final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
                                if (optCommand.isPresent()) {
                                    final CommandAttachment command = optCommand.get();
                                    return Cmd.CLEAR_CONTEXT.equals(command.getCommandType());
                                }
                            }
                            return false;
                        }
                )
                .answer(message -> {
                    chatGptService.closeChat(chatInfo.getChatId());
                    chatInfo = chatGptService.createChat();
                    return boxAnswer("\uD83E\uDDF9 Discussion context cleared successfully");
                })
                .build();
    }

    @Unit(value = UnitName.PROMPT, main = true)
    public AnswerText<Mail> prompt() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            if (mail.getFromPersonId().equals(appProperty.getTelegramId())) {
                                final List<Attachment> attachments = mail.getAttachments();
                                final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
                                if (optCommand.isPresent()) {
                                    final CommandAttachment command = optCommand.get();
                                    return Cmd.PROMPT.equals(command.getCommandType());
                                }
                            }
                            return false;
                        }
                )
                .answer(
                        mail -> {
                            final CommandAttachment promptCommand = Attachments.findFirstCommand(mail.getAttachments()).get();
                            final Optional<String> optPrompt = promptCommand.getArg();
                            if (optPrompt.isPresent()) {
                                final String prompt = optPrompt.get();
                                final GptResponse gptResponse = gptClient.execute(
                                        GptRequest.builder()
                                                .model(GPT3Model.GPT_3_5_TURBO)
                                                .message(GptMessage.fromUser(prompt))
                                                .build()
                                );
                                final List<Choice> choices = gptResponse.getChoices();
                                return boxAnswer(choices.get(choices.size() - 1).getMessage().getContent());
                            }
                            return BoxAnswer.builder().build();
                        }
                )
                .build();
    }

    @Unit(value = UnitName.HELP, main = true)
    public AnswerText<Mail> help() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            if (mail.getFromPersonId().equals(appProperty.getTelegramId())) {
                                final List<Attachment> attachments = mail.getAttachments();
                                final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
                                if (optCommand.isPresent()) {
                                    final CommandAttachment command = optCommand.get();
                                    return Cmd.HELP.equals(command.getCommandType());
                                }
                            }
                            return false;
                        }
                )
                .answer(boxAnswer(Strings.escapeMarkdown("""
                        All correspondence is conducted within one chat. This allows ChatGPT to understand the context of the questions. The context is 100 messages (questions and answers).
                                                
                        Available commands:
                                                
                        /clear_context - Clears the conversation context. In fact, it deletes the chat and creates a new one.
                             
                        /prompt your_question - Allows you to ask a question outside the context of the main conversation.
                        """)))
                .build();
    }

}
