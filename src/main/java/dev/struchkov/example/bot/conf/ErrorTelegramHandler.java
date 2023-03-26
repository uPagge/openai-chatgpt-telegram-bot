package dev.struchkov.example.bot.conf;

import dev.struchkov.godfather.main.domain.content.Message;
import dev.struchkov.godfather.simple.context.service.ErrorHandler;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static dev.struchkov.haiti.utils.Strings.escapeMarkdown;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorTelegramHandler implements ErrorHandler {

    private final AppProperty appProperty;
    private final TelegramSending sending;

    @Override
    public void handle(Message message, Throwable e) {
        log.error(e.getMessage(), e);
        final String errorMessage = escapeMarkdown(e.getMessage());

        final String recipientTelegramId;
        if (appProperty.getTelegramIds().contains(message.getFromPersonId())) {
            recipientTelegramId = message.getFromPersonId();
        } else {
            recipientTelegramId = appProperty.getTelegramIds().get(0);
        }

        sending.send(
                BoxAnswer.builder()
                        .message("Error message:\n\n" + errorMessage)
                        .recipientPersonId(recipientTelegramId)
                        .build()
        );
    }

}
