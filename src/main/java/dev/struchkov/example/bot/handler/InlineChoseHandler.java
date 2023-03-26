package dev.struchkov.example.bot.handler;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.example.bot.service.InlineStorage;
import dev.struchkov.godfather.main.domain.EventContainer;
import dev.struchkov.godfather.simple.context.service.EventHandler;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import dev.struchkov.openai.context.GPTClient;
import dev.struchkov.openai.domain.common.GptMessage;
import dev.struchkov.openai.domain.model.gpt.GPT3Model;
import dev.struchkov.openai.domain.request.GptRequest;
import dev.struchkov.openai.domain.response.Choice;
import dev.struchkov.openai.domain.response.GptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static dev.struchkov.godfather.telegram.main.context.BoxAnswerPayload.ENABLE_MARKDOWN;

@Slf4j
@Component
@RequiredArgsConstructor
public class InlineChoseHandler implements EventHandler<ChosenInlineQuery> {

    private final GPTClient gptClient;
    private final InlineStorage inlineStorage;
    private final AppProperty appProperty;
    private final TelegramSending telegramSending;

    @Override
    public void handle(EventContainer<ChosenInlineQuery> event) {
        final ChosenInlineQuery chosenInlineQuery = event.getObject();
        final String personId = chosenInlineQuery.getFrom().getId().toString();

        if (appProperty.getTelegramIds().contains(personId)) {
            final Optional<String> optRequest = inlineStorage.getQuery(chosenInlineQuery.getResultId());
            if (optRequest.isPresent()) {
                final String question = optRequest.get();
                final GptResponse gptResponse = gptClient.execute(
                        GptRequest.builder()
                                .model(GPT3Model.GPT_3_5_TURBO)
                                .message(GptMessage.fromUser(question))
                                .build()
                );
                final List<Choice> choices = gptResponse.getChoices();
                final String answer = choices.get(choices.size() - 1).getMessage().getContent();
                telegramSending.replaceInlineMessage(
                        chosenInlineQuery.getInlineMessageId(),
                        BoxAnswer.builder()
                                .message(MessageFormat.format("Your question to ChatGPT:\n{0}\n\nAnswer ChatGpt:\n{1}\n-- -- -- -- --\n[Launch your personal ChatGPT Telegram bot](https://docs.struchkov.dev/chatgpt-telegram-bot/en/latest/)", question, answer))
                                .payload(ENABLE_MARKDOWN)
                                .build()
                );
            }
        }
    }

    @Override
    public Class<ChosenInlineQuery> getEventClass() {
        return ChosenInlineQuery.class;
    }

}
