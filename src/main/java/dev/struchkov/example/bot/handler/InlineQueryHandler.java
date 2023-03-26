package dev.struchkov.example.bot.handler;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.example.bot.service.InlineStorage;
import dev.struchkov.godfather.main.domain.EventContainer;
import dev.struchkov.godfather.simple.context.service.EventHandler;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramConnect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InlineQueryHandler implements EventHandler<InlineQuery> {

    private final InlineStorage inlineStorage;
    private final AppProperty appProperty;
    private final TelegramConnect telegramConnect;

    @Override
    public void handle(EventContainer<InlineQuery> event) {
        final InlineQuery inlineQuery = event.getObject();
        if (appProperty.getTelegramIds().contains(inlineQuery.getFrom().getId().toString())) {

            final InputTextMessageContent buildMessageContent = InputTextMessageContent.builder()
                    .messageText(inlineQuery.getQuery())
                    .build();

            try {
                buildMessageContent.validate();
            } catch (TelegramApiValidationException e) {
                log.error(e.getMessage(), e);
            }

            final String id = UUID.randomUUID().toString();
            inlineStorage.save(id, inlineQuery.getQuery());

            final InlineQueryResultArticle result = InlineQueryResultArticle.builder()
                    .id(id)
                    .title("Your request to ChatGPT:")
                    .description(inlineQuery.getQuery())
                    .hideUrl(true)
                    .thumbUrl("https://struchkov.dev/static/img/openai.jpeg")
                    .replyMarkup(
                            InlineKeyboardMarkup.builder()
                                    .keyboard(List.of(List.of(
                                            InlineKeyboardButton.builder()
                                                    .text("Wait... The answer is generated...")
                                                    .callbackData("inline_query")
                                                    .build()
                                    )))
                                    .build()
                    )
                    .inputMessageContent(buildMessageContent).build();

            try {
                result.validate();
            } catch (TelegramApiValidationException e) {
                log.error(e.getMessage(), e);
            }

            final AnswerInlineQuery answerInlineQuery = AnswerInlineQuery.builder()
                    .inlineQueryId(inlineQuery.getId())
                    .result(result)
                    .build();

            try {
                answerInlineQuery.validate();
            } catch (TelegramApiValidationException e) {
                log.error(e.getMessage(), e);
            }

            try {
                telegramConnect.getAbsSender().execute(answerInlineQuery);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Class<InlineQuery> getEventClass() {
        return InlineQuery.class;
    }

}
