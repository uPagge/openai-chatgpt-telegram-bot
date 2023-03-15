package dev.struchkov.example.bot.unit;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.godfather.main.domain.keyboard.button.SimpleButton;
import dev.struchkov.godfather.simple.domain.BoxAnswer;
import dev.struchkov.godfather.simple.domain.SentBox;
import dev.struchkov.godfather.telegram.domain.keyboard.InlineKeyBoard;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Optional;

import static dev.struchkov.godfather.telegram.main.context.BoxAnswerPayload.DISABLE_WEB_PAGE_PREVIEW;
import static dev.struchkov.haiti.utils.Checker.checkNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartNotify {

    private final OkHttpClient client = new OkHttpClient();

    private final TelegramSending sending;
    private final TelegramService telegramService;
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
                                👨‍💻 Developer: [Struchkov Mark](https://mark.struchkov.dev/)
                                💊 Docs: https://docs.struchkov.dev/chatgpt-telegram-bot
                                """,
                        appProperty.getVersion()
                ))
                .keyBoard(InlineKeyBoard.inlineKeyBoard(SimpleButton.simpleButton("❤️ Support Develop", "support")))
                .payload(DISABLE_WEB_PAGE_PREVIEW, true)
                .build();
        boxAnswer.setRecipientIfNull(appProperty.getTelegramId());
        sending.send(boxAnswer);
        sendNotice();
    }

    /**
     * Используется для уведомления пользователя о выходе новой версии.
     */
    private void sendNotice() {
        final String requestUrl = "https://metrika.struchkov.dev/gitlab-notify/start-notice/chatgpt?version=" + appProperty.getVersion();
        final Request request = new Request.Builder().get().url(requestUrl).build();
        try {
            final Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                final String noticeMessage = response.body().string();
                if (checkNotBlank(noticeMessage)) {
                    final BoxAnswer notice = BoxAnswer.builder()
                            .message(noticeMessage)
                            .recipientPersonId(appProperty.getTelegramId())
                            .payload(DISABLE_WEB_PAGE_PREVIEW, true)
                            .build();
                    final Optional<SentBox> optSentBox = sending.send(notice);
                    if (optSentBox.isPresent()) {
                        final SentBox sentBox = optSentBox.get();
                        final String messageId = sentBox.getMessageId();
                        telegramService.pinMessage(appProperty.getTelegramId(), messageId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

}
