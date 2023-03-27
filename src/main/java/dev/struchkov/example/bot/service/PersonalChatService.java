package dev.struchkov.example.bot.service;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.haiti.context.exception.NotFoundException;
import dev.struchkov.openai.context.ChatGptService;
import dev.struchkov.openai.domain.chat.ChatInfo;
import dev.struchkov.openai.domain.chat.CreateChat;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dev.struchkov.haiti.context.exception.NotFoundException.notFoundException;

@Service
public class PersonalChatService {

    public static final String DEFAULT_CHAT_NAME = "DEFAULT";

    private final ChatGptService chatGptService;

    // ключ это идентификатор пользователя, значение это название чата
    private final Map<String, String> currentChatMap = new ConcurrentHashMap<>();

    // первый ключ это идентификатор пользователя. второй это название чата
    private final Map<String, Map<String, ChatInfo>> chatMap = new ConcurrentHashMap<>();

    public PersonalChatService(
            AppProperty appProperty,
            ChatGptService chatGptService
    ) {
        this.chatGptService = chatGptService;
        appProperty.getTelegramIds().forEach(
                telegramId -> {
                    final ChatInfo defaultChat = this.chatGptService.createChat(CreateChat.builder().build());
                    final HashMap<String, ChatInfo> personChats = new HashMap<>();
                    personChats.put(DEFAULT_CHAT_NAME, defaultChat);
                    chatMap.put(telegramId, personChats);
                    currentChatMap.put(telegramId, DEFAULT_CHAT_NAME);
                }
        );
    }

    public ChatInfo createChat(String personId, String chatName) {
        if (existChat(personId, chatName)) {
            closeChat(personId, chatName, DEFAULT_CHAT_NAME);
        }
        final ChatInfo newChat = chatGptService.createChat(CreateChat.builder().build());
        chatMap.get(personId).put(chatName, newChat);
        return newChat;
    }

    public String getCurrentChatName(String personId) {
        return currentChatMap.get(personId);
    }

    public ChatInfo getCurrentChat(String personId) {
        final String currentChatName = getCurrentChatName(personId);
        return chatMap.get(personId).get(currentChatName);
    }

    public void setBehavior(String personId, String chatName, String behavior) {
        final ChatInfo chatInfo = getChat(personId, chatName);
        chatInfo.setSystemBehavior(behavior);
        chatGptService.updateChat(chatInfo);
    }

    public ChatInfo getChat(String personId, String chatName) {
        return Optional.ofNullable(chatMap.get(personId).get(chatName))
                .orElseThrow(notFoundException("Chat with this name was not found"));
    }

    public boolean existChat(String personId, String chatName) {
        return chatMap.get(personId).containsKey(chatName);
    }

    public void closeChat(String personId, String closeChatName, String switchChatName) {
        if (DEFAULT_CHAT_NAME.equals(closeChatName)) {
            throw new RuntimeException("You can't close the default chat room");
        }

        final ChatInfo chat = getChat(personId, closeChatName);
        final ChatInfo switchChat = getChat(personId, switchChatName);

        currentChatMap.put(personId, switchChatName);
        chatGptService.closeChat(chat.getChatId());
        chatMap.get(personId).remove(closeChatName);
    }

    public void clearContext(String personId, String chatName) {
        final ChatInfo chat = getChat(personId, chatName);
        chatGptService.clearContext(chat.getChatId());
    }

    public void clearBehavior(String personId) {
        final ChatInfo currentChat = getCurrentChat(personId);
        currentChat.setSystemBehavior(null);
        chatGptService.updateChat(currentChat);
    }

    public void switchChat(String personId, String chatName) {
        if (existChat(personId, chatName)) {
            currentChatMap.put(personId, chatName);
        } else {
            throw new NotFoundException("Chat with the name {0} not found", chatName);
        }
    }

    public Set<String> getAllChatName(String personId) {
        return chatMap.get(personId).keySet();
    }

}
