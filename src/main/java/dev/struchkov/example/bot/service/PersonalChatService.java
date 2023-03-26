package dev.struchkov.example.bot.service;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.openai.context.ChatGptService;
import dev.struchkov.openai.domain.chat.ChatInfo;
import dev.struchkov.openai.domain.chat.CreateChat;
import dev.struchkov.openai.domain.chat.UpdateChat;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PersonalChatService {

    private final ChatGptService chatGptService;
    private final Map<String, String> chatDefaultContext = new ConcurrentHashMap<>();
    private final Map<String, ChatInfo> chatMap = new ConcurrentHashMap<>();

    public PersonalChatService(
            AppProperty appProperty,
            ChatGptService chatGptService
    ) {
        this.chatGptService = chatGptService;
        appProperty.getTelegramIds().forEach(
                telegramId -> {
                    final ChatInfo newChat = this.chatGptService.createChat(
                            CreateChat.builder().build()
                    );
                    chatMap.put(telegramId, newChat);
                }
        );
    }

    public ChatInfo getChatByPersonId(String personId) {
        return chatMap.get(personId);
    }

    public ChatInfo recreateChat(String personId) {
        final ChatInfo chatInfo = chatMap.get(personId);
        chatGptService.closeChat(chatInfo.getChatId());
        final ChatInfo newChat = chatGptService.createChat(
                CreateChat.builder()
                        .systemBehavior(chatDefaultContext.get(personId))
                        .build()
        );
        chatMap.put(personId, newChat);
        return newChat;
    }

    public void setBehavior(String fromPersonId, String behavior) {
        chatDefaultContext.put(fromPersonId, behavior);
        final ChatInfo chatInfo = chatMap.get(fromPersonId);
        chatGptService.updateChat(
                UpdateChat.builder()
                        .chatId(chatInfo.getChatId())
                        .systemBehavior(behavior)
                        .build()
        );
    }

}
