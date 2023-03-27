package dev.struchkov.example.bot.conf;

import dev.struchkov.godfather.main.domain.EventContainer;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.context.service.EventDispatching;
import dev.struchkov.godfather.simple.context.service.EventHandler;
import dev.struchkov.godfather.simple.core.EventDispatchingImpl;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.telegram.main.context.MailPayload;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import org.springframework.stereotype.Component;

import java.util.List;

import static dev.struchkov.godfather.telegram.main.context.BoxAnswerPayload.ENABLE_MARKDOWN;

@Component
public class CustomEventDispatching extends EventDispatchingImpl implements EventDispatching {

    private final TelegramSending telegramSending;
    private final AppProperty appProperty;

    public CustomEventDispatching(List<EventHandler> eventProviders, TelegramSending telegramSending, AppProperty appProperty) {
        super(eventProviders);
        this.telegramSending = telegramSending;
        this.appProperty = appProperty;
    }

    @Override
    public void dispatch(EventContainer event) {
        if (Mail.class.equals(event.getType())) {
            final Mail mail = (Mail) event.getObject();
            final String fromPersonId = mail.getFromPersonId();
            if (appProperty.getTelegramIds().contains(fromPersonId)) {
                super.dispatch(event);
            } else {
                final StringBuilder messageText = new StringBuilder("\uD83D\uDEA8 *Attempted unauthorized access to the bot*")
                        .append("\n-- -- -- -- --\n");

                mail.getPayLoad(MailPayload.USERNAME)
                        .ifPresent(username -> messageText.append("\uD83E\uDDB9\u200D♂️: @").append(username));

                messageText.append("\n")
                        .append("\uD83D\uDCAC: ").append(mail.getText());

                for (String adminTelegramId : appProperty.getAdminTelegramIds()) {
                    telegramSending.send(
                            BoxAnswer.builder()
                                    .recipientPersonId(adminTelegramId)
                                    .message(messageText.toString())
                                    .payload(ENABLE_MARKDOWN)
                                    .build()
                    );
                }

            }
        } else {
            super.dispatch(event);
        }
    }

}
