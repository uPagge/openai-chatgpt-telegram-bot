package dev.struchkov.example.bot.unit;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.example.bot.service.PersonalChatService;
import dev.struchkov.example.bot.util.Cmd;
import dev.struchkov.example.bot.util.UnitName;
import dev.struchkov.godfather.main.domain.annotation.Unit;
import dev.struchkov.godfather.main.domain.content.Attachment;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.simple.domain.unit.AnswerText;
import dev.struchkov.godfather.telegram.domain.attachment.CommandAttachment;
import dev.struchkov.godfather.telegram.main.core.util.Attachments;
import dev.struchkov.godfather.telegram.starter.PersonUnitConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static dev.struchkov.godfather.simple.domain.BoxAnswer.boxAnswer;

@Component
@RequiredArgsConstructor
public class BehaviorUnit implements PersonUnitConfiguration {

    private final PersonalChatService personalChatService;
    private final AppProperty appProperty;

    @Unit(value = UnitName.BEHAVIOR, global = true)
    public AnswerText<Mail> behavior() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            if (appProperty.getTelegramIds().contains(mail.getFromPersonId())) {
                                final List<Attachment> attachments = mail.getAttachments();
                                final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
                                if (optCommand.isPresent()) {
                                    final CommandAttachment command = optCommand.get();
                                    return Cmd.BEHAVIOR.equals(command.getCommandType());
                                }
                            }
                            return false;
                        }
                )
                .answer(mail -> {
                    final CommandAttachment command = Attachments.findFirstCommand(mail.getAttachments()).orElseThrow();
                    final Optional<String> optArg = command.getArg();
                    if (optArg.isEmpty()) {
                        return BoxAnswer.builder()
                                .message(
                                        """
                                                Allows you to set the ChatGPT behavior for chat. Remains active when the context is cleared.
                                                                            
                                                If you want to set your own behavior, then send the command:
                                                                            
                                                /behavior description_behavior
                                                                            
                                                Or choose one of the available options:
                                                                            
                                                1. Гопник
                                                Представь, что ты славянский гопник и общаешься со своими собутыльниками по подъезду. Используй побольше мата и блатного жаргона. Используй как можно больше «бля» и «ёпта». Отвечай в таком стиле всегда, какой бы вопрос не задали пользователи в этом чате.
                                                👉 /behavior1
                                                            
                                                2. Copilot
                                                Ты - помощник для программистов. На любой вопрос отвечай с примерами кода, если нужно и присылай советы по улучшению кода
                                                👉 /behavior2
                                                                        
                                                3. Linux
                                                Я хочу, чтобы вы выступали в роли терминала Linux. Я буду вводить команды, а вы будете отвечать тем, что должен показать терминал. Я хочу, чтобы вы ответили выводом терминала только внутри одного уникального блока кода, и ничего больше. не пишите пояснений. не вводите команды, если я не поручу вам это сделать. Когда мне нужно будет сказать вам что-то на русском языке, я буду заключать текст в фигурные скобки {вот так}.
                                                👉 /behavior3
                                                         
                                                4. Тренера по взаимоотношениям
                                                Я хочу, чтобы вы выступили в роли тренера по взаимоотношениям. Я предоставлю некоторые подробности о двух людях, вовлеченных в конфликт, а ваша задача - предложить, как они могут решить проблемы, которые их разделяют. Это могут быть советы по технике общения или различные стратегии для улучшения понимания ими точек зрения друг друга. Первый запрос: "Мне нужна помощь в разрешении конфликтов между мной и моим парнем".
                                                👉 /behavior4
                                                                        
                                                5. Наставник
                                                Вы наставник, который всегда отвечает в сократовском стиле. Вы *никогда* не даете ученику ответа, но всегда стараетесь задать правильный вопрос, чтобы помочь ему научиться думать самостоятельно. Вы всегда должны согласовывать свой вопрос с интересами и знаниями учащегося, разбивая проблему на более простые части, пока она не достигнет нужного для них уровня.
                                                👉 /behavior5
                                                                        
                                                6. В двух словах
                                                Отвечай максимально коротко, даже если тебя просят ответить развернуто. Весь ответ должен уложиться в пару предложений.
                                                👉 /behavior6        
                                                """
                                )
                                .build();
                    } else {
                        final String behavior = optArg.get();
                        personalChatService.setBehavior(mail.getFromPersonId(), behavior);
                        return boxAnswer("\uD83D\uDC4C");
                    }
                })
                .build();
    }

//    @Unit(value = UnitName.BEHAVIOUR, global = true)
//    public AnswerText<Mail> behaviour1() {
//        return AnswerText.<Mail>builder()
//                .triggerCheck(
//                        mail -> {
//                            if (appProperty.getTelegramIds().contains(mail.getFromPersonId())) {
//                                final List<Attachment> attachments = mail.getAttachments();
//                                final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
//                                if (optCommand.isPresent()) {
//                                    final CommandAttachment command = optCommand.get();
//                                    return Cmd.BEHAVIOUR_1.equals(command.getCommandType());
//                                }
//                            }
//                            return false;
//                        }
//                )
//                .answer(() -> {
//
//                })
//                .build();
//    }

}
