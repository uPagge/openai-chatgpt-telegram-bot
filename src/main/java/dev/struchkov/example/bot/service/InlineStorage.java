package dev.struchkov.example.bot.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class InlineStorage {

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private final Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public InlineStorage() {
        scheduledExecutor.scheduleAtFixedRate(cache::cleanUp, 1, 1, TimeUnit.MINUTES);
    }

    public void save(String inlineId, String query) {
        cache.put(inlineId, query);
    }

    public Optional<String> getQuery(String inlineId) {
        return Optional.ofNullable(cache.getIfPresent(inlineId));
    }

}
