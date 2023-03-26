package dev.struchkov.example.bot.conf;

import dev.struchkov.openai.ChatGptServiceImpl;
import dev.struchkov.openai.GPTClientImpl;
import dev.struchkov.openai.OpenAIClientImpl;
import dev.struchkov.openai.context.ChatGptService;
import dev.struchkov.openai.context.GPTClient;
import dev.struchkov.openai.context.OpenAIClient;
import dev.struchkov.openai.data.local.ChatGptLocalStorage;
import dev.struchkov.openai.domain.conf.GPTConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConf {

    @Bean
    @ConfigurationProperties("openai")
    public GPTConfig gptConfig() {
        return new GPTConfig();
    }

    @Bean
    public GPTClient gptClient(GPTConfig gptConfig) {
        return new GPTClientImpl(gptConfig);
    }

    @Bean
    public OpenAIClient openAIClient(GPTConfig gptConfig) {
        return new OpenAIClientImpl(gptConfig);
    }

    @Bean
    public ChatGptService chatGptService(GPTClient gptClient) {
        return new ChatGptServiceImpl(
                gptClient,
                new ChatGptLocalStorage()
        );
    }

}
