//package dev.struchkov.example.bot.unit;
//
//import dev.struchkov.example.bot.util.Cmd;
//import dev.struchkov.example.bot.util.UnitName;
//import dev.struchkov.godfather.main.domain.annotation.Unit;
//import dev.struchkov.godfather.main.domain.content.Attachment;
//import dev.struchkov.godfather.main.domain.content.ChatMail;
//import dev.struchkov.godfather.simple.domain.BoxAnswer;
//import dev.struchkov.godfather.simple.domain.unit.AnswerText;
//import dev.struchkov.godfather.telegram.domain.attachment.CommandAttachment;
//import dev.struchkov.godfather.telegram.main.core.util.Attachments;
//import dev.struchkov.godfather.telegram.starter.ChatUnitConfiguration;
//import dev.struchkov.openai.context.GPTClient;
//import dev.struchkov.openai.domain.common.GptMessage;
//import dev.struchkov.openai.domain.model.gpt.GPT3Model;
//import dev.struchkov.openai.domain.request.GptRequest;
//import dev.struchkov.openai.domain.response.Choice;
//import dev.struchkov.openai.domain.response.GptResponse;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Optional;
//
//import static dev.struchkov.godfather.simple.domain.BoxAnswer.boxAnswer;
//
//@Component
//public class GroupChatGPTUnit implements ChatUnitConfiguration {
//
//    private final GPTClient gptClient;
//
//    public GroupChatGPTUnit(
//            GPTClient gptClient
//    ) {
//        this.gptClient = gptClient;
//    }
//
//    @Unit(value = UnitName.PROMPT, main = true)
//    public AnswerText<ChatMail> prompt() {
//        return AnswerText.<ChatMail>builder()
//                .triggerCheck(
//                        mail -> {
//                            final List<Attachment> attachments = mail.getAttachments();
//                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
//                            if (optCommand.isPresent()) {
//                                final CommandAttachment command = optCommand.get();
//                                return Cmd.GPT.equals(command.getCommandType());
//                            }
//                            return false;
//                        }
//                )
//                .answer(
//                        mail -> {
//                            final CommandAttachment promptCommand = Attachments.findFirstCommand(mail.getAttachments()).get();
//                            final Optional<String> optPrompt = promptCommand.getArg();
//                            if (optPrompt.isPresent()) {
//                                final String prompt = optPrompt.get();
//                                final GptResponse gptResponse = gptClient.execute(
//                                        GptRequest.builder()
//                                                .model(GPT3Model.GPT_3_5_TURBO)
//                                                .message(GptMessage.fromUser(prompt))
//                                                .build()
//                                );
//                                final List<Choice> choices = gptResponse.getChoices();
//                                return boxAnswer(choices.get(choices.size() - 1).getMessage().getContent());
//                            }
//                            return BoxAnswer.builder().build();
//                        }
//                )
//                .build();
//    }
//
//}
