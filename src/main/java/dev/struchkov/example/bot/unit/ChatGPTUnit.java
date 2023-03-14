package dev.struchkov.example.bot.unit;

import dev.struchkov.godfather.main.domain.annotation.Unit;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.domain.unit.AnswerText;
import dev.struchkov.godfather.telegram.domain.ChatAction;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramService;
import dev.struchkov.godfather.telegram.starter.UnitConfiguration;
import dev.struchkov.openai.context.ChatGptService;
import dev.struchkov.openai.context.GPTClient;
import dev.struchkov.openai.domain.chat.ChatInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static dev.struchkov.example.bot.util.UnitName.GENERAL_MENU;
import static dev.struchkov.godfather.simple.domain.BoxAnswer.boxAnswer;

@Component
@RequiredArgsConstructor
public class ChatGPTUnit implements UnitConfiguration {

    private ChatInfo test;

    private final TelegramService telegramService;
    private final GPTClient gptClient;
    private final ChatGptService chatGptService;

    @Unit(value = GENERAL_MENU, main = true)
    public AnswerText<Mail> chatGpt() {
        return AnswerText.<Mail>builder()
                .answer(message -> {
                    if (test == null) {
                        test = chatGptService.createChat();
                    }

                    telegramService.executeAction(message.getPersonId(), ChatAction.TYPING);
                    final String answerText = chatGptService.sendNewMessage(test.getChatId(), message.getText());

                    return boxAnswer(answerText);
                })
                .build();
    }

}
