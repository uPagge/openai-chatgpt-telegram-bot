package dev.struchkov.example.bot.unit;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static dev.struchkov.godfather.telegram.main.context.BoxAnswerPayload.DISABLE_WEB_PAGE_PREVIEW;

@Component
@RequiredArgsConstructor
public class StartNotify {

    private final TelegramSending sending;
    private final AppProperty appProperty;

    @PostConstruct
    public void sendStartNotify() {
        final BoxAnswer boxAnswer = BoxAnswer.builder()
                .message(MessageFormat.format(
                        """
                        Hello 👋
                        Your personal ChatGPT bot has been successfully launched.
                        
                        Use the help command to find out about the possibilities 🚀
                        -- -- -- -- --
                        🤘 Version: {0}
                        👨‍💻 Developer: [https://mark.struchkov.dev/](Struchkov Mark)
                        💊 Docs: https://docs.struchkov.dev/chatgpt-telegram-bot
                        """,
                        appProperty.getVersion()
                ))
                .payload(DISABLE_WEB_PAGE_PREVIEW, true)
                .build();
        boxAnswer.setRecipientIfNull(appProperty.getTelegramId());
        sending.send(boxAnswer);
    }

}
