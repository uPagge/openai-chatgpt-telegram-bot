package dev.struchkov.example.bot.unit;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.example.bot.service.PersonalChatService;
import dev.struchkov.example.bot.util.Cmd;
import dev.struchkov.example.bot.util.UnitName;
import dev.struchkov.godfather.main.domain.annotation.Unit;
import dev.struchkov.godfather.main.domain.content.Attachment;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.main.domain.keyboard.button.SimpleButton;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.simple.domain.SentBox;
import dev.struchkov.godfather.simple.domain.unit.AnswerText;
import dev.struchkov.godfather.telegram.domain.ChatAction;
import dev.struchkov.godfather.telegram.domain.ClientBotCommand;
import dev.struchkov.godfather.telegram.domain.attachment.ButtonClickAttachment;
import dev.struchkov.godfather.telegram.domain.attachment.CommandAttachment;
import dev.struchkov.godfather.telegram.domain.keyboard.InlineKeyBoard;
import dev.struchkov.godfather.telegram.main.core.util.Attachments;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramService;
import dev.struchkov.godfather.telegram.starter.PersonUnitConfiguration;
import dev.struchkov.haiti.utils.Strings;
import dev.struchkov.openai.context.ChatGptService;
import dev.struchkov.openai.context.GPTClient;
import dev.struchkov.openai.domain.chat.ChatInfo;
import dev.struchkov.openai.domain.common.GptMessage;
import dev.struchkov.openai.domain.message.AnswerChatMessage;
import dev.struchkov.openai.domain.model.gpt.GPT3Model;
import dev.struchkov.openai.domain.request.GptRequest;
import dev.struchkov.openai.domain.response.Choice;
import dev.struchkov.openai.domain.response.GptResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static dev.struchkov.example.bot.util.UnitName.CLEAR_CONTEXT;
import static dev.struchkov.example.bot.util.UnitName.GPT_UNIT;
import static dev.struchkov.godfather.simple.domain.BoxAnswer.boxAnswer;
import static dev.struchkov.godfather.telegram.main.context.BoxAnswerPayload.DISABLE_WEB_PAGE_PREVIEW;
import static dev.struchkov.godfather.telegram.main.context.BoxAnswerPayload.ENABLE_MARKDOWN;
import static java.text.MessageFormat.format;

@Component
@RequiredArgsConstructor
public class PersonalChatGPTUnit implements PersonUnitConfiguration {

    private final PersonalChatService personalChatService;
    private final TelegramSending telegramSending;
    private final TelegramService telegramService;
    private final AppProperty appProperty;
    private final GPTClient gptClient;
    private final ChatGptService chatGptService;

    @PostConstruct
    public void createCommands() {
        telegramService.addCommand(List.of(
                ClientBotCommand.builder()
                        .key(Cmd.HELP)
                        .description("help in use")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.CHAT)
                        .description("Create or toggle a chat room")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.CLOSE_CHAT)
                        .description("Close the current chat.")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.CURRENT_CHAT)
                        .description("Returns the name of the current chat")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.CLEAR_CONTEXT)
                        .description("Clears the discussion context. Start a conversation from the beginning.")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.BALANCE)
                        .description("Find out how much you spent this month.")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.BEHAVIOR)
                        .description("Allows you to set the initial behavior of ChatGPT.")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.CURRENT_BEHAVIOR)
                        .description("Returns the current behavior description for the chat")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.CLEAR_BEHAVIOR)
                        .description("Clears the behavior settings for the current chat")
                        .build(),

                ClientBotCommand.builder()
                        .key(Cmd.SUPPORT_DEV)
                        .description("Support project development.")
                        .build()
        ));
    }

    @Unit(value = GPT_UNIT, main = true, global = true)
    public AnswerText<Mail> chatGpt() {
        return AnswerText.<Mail>builder()
                .triggerCheck(mail -> Attachments.findFirstCommand(mail.getAttachments()).isEmpty())
                .answer(message -> {
                    final ChatInfo chatInfo = personalChatService.getCurrentChat(message.getFromPersonId());
                    final long countMessages = chatGptService.getCountMessages(chatInfo.getChatId());

                    final StringBuilder builder = new StringBuilder();
                    builder.append("Wait... Response is being generated...\nIt might take a long time ⏳");

                    if (countMessages > 40) {
                        builder.append(Strings.escapeMarkdown("\n-- -- -- -- --\nWe recommend periodically clearing the conversation context (/clear_context). If this is not done, then the memory resources on your PC will run out."));
                    }

                    final BoxAnswer answerWait = BoxAnswer.builder()
                            .recipientPersonId(message.getFromPersonId())
                            .message(builder.toString())
                            .build();
                    final Optional<SentBox> optSentBox = telegramSending.send(answerWait);

                    telegramService.executeAction(message.getFromPersonId(), ChatAction.TYPING);

                    final AnswerChatMessage answer = chatGptService.sendNewMessage(chatInfo.getChatId(), message.getText());
                    if (optSentBox.isPresent()) {
                        final SentBox sentBox = optSentBox.get();
                        telegramSending.replaceMessage(sentBox.getPersonId(), sentBox.getMessageId(), boxAnswer(format("\uD83D\uDC47 Answer received. Request cost: {0} tokens", answer.getUsage().getTotalTokens())));
                    }
                    return boxAnswer(answer.getMessage());
                })
                .priority(5)
                .build();
    }

    @Unit(value = CLEAR_CONTEXT, global = true)
    public AnswerText<Mail> clearContext() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final List<Attachment> attachments = mail.getAttachments();
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
                            return optCommand.filter(commandAttachment -> Cmd.CLEAR_CONTEXT.equals(commandAttachment.getCommandType())).isPresent();
                        }
                )
                .answer(message -> {
                    final String personId = message.getFromPersonId();
                    final String currentChatName = personalChatService.getCurrentChatName(personId);
                    personalChatService.clearContext(personId, currentChatName);
                    return boxAnswer("\uD83E\uDDF9 Discussion context cleared successfully");
                })
                .build();
    }

    @Unit(value = UnitName.START, global = true)
    public AnswerText<Mail> startMessage() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(mail.getAttachments());
                            return optCommand.filter(commandAttachment -> Cmd.START.equals(commandAttachment.getCommandType())).isPresent();
                        }
                )
                .answer(message -> {
                    return BoxAnswer.builder()
                            .message(format(
                                    """
                                            Hello 👋
                                            Your personal ChatGPT bot has been successfully launched.
                                                                    
                                            Use the help command to find out about the possibilities 🚀
                                            -- -- -- -- --
                                            🤘 Version: {0}
                                            👨‍💻 Developer: [Struchkov Mark](https://mark.struchkov.dev/)
                                            💊 Docs: https://docs.struchkov.dev/chatgpt-telegram-bot
                                            """,
                                    appProperty.getVersion()
                            ))
                            .keyBoard(InlineKeyBoard.inlineKeyBoard(SimpleButton.simpleButton("❤️ Support Develop", "support")))
                            .payload(DISABLE_WEB_PAGE_PREVIEW)
                            .payload(ENABLE_MARKDOWN)
                            .build();
                })
                .build();
    }

    @Unit(value = UnitName.PROMPT, global = true)
    public AnswerText<Mail> prompt() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(mail.getAttachments());
                            return optCommand.filter(commandAttachment -> Cmd.PROMPT.equals(commandAttachment.getCommandType())).isPresent();
                        }
                )
                .answer(
                        message -> {
                            final CommandAttachment promptCommand = Attachments.findFirstCommand(message.getAttachments()).get();
                            final Optional<String> optPrompt = promptCommand.getArg();
                            if (optPrompt.isPresent()) {
                                final String prompt = optPrompt.get();

                                final BoxAnswer answerWait = BoxAnswer.builder()
                                        .recipientPersonId(message.getFromPersonId())
                                        .message("Wait... Response is being generated...\nIt might take a long time ⏳")
                                        .build();
                                final Optional<SentBox> optSentBox = telegramSending.send(answerWait);

                                telegramService.executeAction(message.getFromPersonId(), ChatAction.TYPING);

                                final GptResponse gptResponse = gptClient.execute(
                                        GptRequest.builder()
                                                .model(GPT3Model.GPT_3_5_TURBO)
                                                .message(GptMessage.fromUser(prompt))
                                                .build()
                                );

                                if (optSentBox.isPresent()) {
                                    final SentBox sentBox = optSentBox.get();
                                    telegramSending.replaceMessage(sentBox.getPersonId(), sentBox.getMessageId(), boxAnswer(format("\uD83D\uDC47 Answer received. Request cost: {0} tokens", gptResponse.getUsage().getTotalTokens())));
                                }

                                final List<Choice> choices = gptResponse.getChoices();
                                final String answer = choices.get(choices.size() - 1).getMessage().getContent();
                                return BoxAnswer.builder()
                                        .message(answer)
                                        .payload(ENABLE_MARKDOWN)
                                        .build();
                            }
                            return BoxAnswer.builder().build();
                        }
                )
                .build();
    }

    @Unit(value = UnitName.SUPPORT, global = true)
    public AnswerText<Mail> support() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final List<Attachment> attachments = mail.getAttachments();
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
                            if (optCommand.isPresent()) {
                                return Cmd.SUPPORT_DEV.equals(optCommand.get().getCommandType());
                            }

                            final Optional<ButtonClickAttachment> optClick = Attachments.findFirstButtonClick(attachments);
                            if (optClick.isPresent()) {
                                return Cmd.SUPPORT_DEV.equals(optClick.get().getRawCallBackData());
                            }
                            return false;
                        }
                )
                .answer(
                        () -> BoxAnswer.builder()
                                .message("""
                                        ❤️ *Support Develop*
                                                                        
                                        Sponsorship makes a project sustainable because it pays for the time of the maintainers of that project, a very scarce resource that is spent on developing new features, fixing bugs, improving stability, solving problems, and general support. *The biggest bottleneck in Open Source is time.*
                                                           
                                        Bank card (Russia): [https://www.tinkoff.ru/cf/4iU6NB3uzqx](https://www.tinkoff.ru/cf/4iU6NB3uzqx)
                                                                        
                                        TON: `struchkov-mark.ton`
                                                                        
                                        BTC:
                                        `bc1pt49vnp43c4mktk6309zlq3020dzd0p89gc8d90zzn4sgjvck56xs0t86vy`
                                                                        
                                        ETH (USDT, DAI, USDC):
                                        `0x7668C802Bd71Be965671D4Bbb1AD90C7f7f32921`
                                                                        
                                        BNB (USDT, DAI, USDC):
                                        `0xDa41aC95f606850f2E01ba775e521Cd385AA7D03`
                                        """)
                                .payload(ENABLE_MARKDOWN)
                                .build()
                )
                .build();
    }

    @Unit(value = UnitName.HELP, global = true)
    public AnswerText<Mail> help() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(mail.getAttachments());
                            return optCommand.filter(commandAttachment -> Cmd.HELP.equals(commandAttachment.getCommandType())).isPresent();
                        }
                )
                .answer(() -> boxAnswer("""
                        All correspondence is conducted within one chat. This allows ChatGPT to understand the context of the questions. The context is 100 messages (questions and answers).
                                                
                        Available commands:
                                                
                        /clear_context - Clears the conversation context. In fact, it deletes the chat and creates a new one.
                             
                        /prompt your_question - Allows you to ask a question outside the context of the main conversation.
                        """))
                .build();
    }

}
