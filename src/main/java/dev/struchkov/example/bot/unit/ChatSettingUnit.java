package dev.struchkov.example.bot.unit;

import dev.struchkov.example.bot.service.PersonalChatService;
import dev.struchkov.example.bot.util.Cmd;
import dev.struchkov.example.bot.util.UnitName;
import dev.struchkov.godfather.main.domain.annotation.Unit;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.simple.domain.unit.AnswerText;
import dev.struchkov.godfather.telegram.domain.attachment.CommandAttachment;
import dev.struchkov.godfather.telegram.main.core.util.Attachments;
import dev.struchkov.godfather.telegram.starter.PersonUnitConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.struchkov.godfather.simple.domain.BoxAnswer.boxAnswer;

@Component
@RequiredArgsConstructor
public class ChatSettingUnit implements PersonUnitConfiguration {

    private final PersonalChatService chatService;

    @Unit(value = UnitName.CHAT, global = true)
    public AnswerText<Mail> chat() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(mail.getAttachments());
                            return optCommand.filter(commandAttachment -> Cmd.CHAT.equals(commandAttachment.getCommandType())).isPresent();
                        }
                )
                .answer(
                        mail -> {
                            final String personId = mail.getFromPersonId();
                            final CommandAttachment command = Attachments.findFirstCommand(mail.getAttachments()).orElseThrow();
                            final Optional<String> optArg = command.getArg();
                            if (optArg.isPresent()) {
                                final String chatName = optArg.get();
                                final boolean isExistChat = chatService.existChat(personId, chatName);
                                if (!isExistChat) {
                                    chatService.createChat(personId, chatName);
                                }
                                chatService.switchChat(personId, chatName);
                                return boxAnswer("Chat successfully switched");
                            } else {
                                final Set<String> chatNames = chatService.getAllChatName(personId);
                                final String chatList;
                                if (chatNames.size() > 1) {
                                    chatList = "\n\nYour chats:\n" + chatNames.stream()
                                            .filter(name -> !"DEFAULT".equals(name))
                                            .collect(Collectors.joining("\n• ", "• ", ""));
                                } else {
                                    chatList = "";
                                }

                                final String message = MessageFormat.format(
                                        """
                                                Chats allow you to have multiple conversations with different discussion contexts.
                                                                                        
                                                To create a new chat room or switch to an existing one, use the
                                                                                        
                                                /chat chat_name{0}                                    
                                                """,
                                        chatList
                                );
                                return BoxAnswer.builder()
                                        .message(message)
                                        .build();
                            }
                        }
                )
                .build();
    }

    @Unit(value = UnitName.CURRENT_CHAT, global = true)
    public AnswerText<Mail> getCurrentChatName() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(mail.getAttachments());
                            return optCommand.filter(commandAttachment -> Cmd.CURRENT_CHAT.equals(commandAttachment.getCommandType())).isPresent();
                        }
                )
                .answer(
                        mail -> {
                            final String currentChatName = chatService.getCurrentChatName(mail.getFromPersonId());
                            return BoxAnswer.builder()
                                    .message("Current chat name: " + currentChatName)
                                    .build();

                        }
                )
                .build();
    }

    @Unit(value = UnitName.CLOSE_CHAT, global = true)
    public AnswerText<Mail> closeChat() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(mail.getAttachments());
                            return optCommand.filter(commandAttachment -> Cmd.CLOSE_CHAT.equals(commandAttachment.getCommandType())).isPresent();
                        }
                )
                .answer(
                        mail -> {
                            final String personId = mail.getFromPersonId();
                            final CommandAttachment command = Attachments.findFirstCommand(mail.getAttachments()).orElseThrow();
                            final Optional<String> optArg = command.getArg();
                            if (optArg.isPresent()) {
                                final String name = optArg.get();
                                chatService.closeChat(personId, name, PersonalChatService.DEFAULT_CHAT_NAME);
                                return boxAnswer("Chat has been successfully closed. You have returned to the default chat room!");
                            } else {
                                final String currentChatName = chatService.getCurrentChatName(personId);
                                chatService.closeChat(personId, currentChatName, PersonalChatService.DEFAULT_CHAT_NAME);
                                return boxAnswer("Chat has been successfully closed. You have returned to the default chat room!");
                            }
                        }
                )
                .build();
    }

}
