package dev.struchkov.example.bot.conf;

import dev.struchkov.openai.ChatGptServiceImpl;
import dev.struchkov.openai.GPTClientImpl;
import dev.struchkov.openai.context.ChatGptService;
import dev.struchkov.openai.context.GPTClient;
import dev.struchkov.openai.data.local.ChatGptLocalStorage;
import dev.struchkov.openai.domain.conf.GPTConfig;
import dev.struchkov.openai.domain.model.gpt.GPT3Model;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConf {

    @Bean
    @ConfigurationProperties("openai")
    public GPTConfig gptConfig() {
        final GPTConfig gptConfig = new GPTConfig();
        gptConfig.setAiModel(GPT3Model.GPT_3_5_TURBO);
        return gptConfig;
    }

    @Bean
    public GPTClient gptClient(GPTConfig gptConfig) {
        return new GPTClientImpl(gptConfig);
    }

    @Bean
    public ChatGptService chatGptService(
            GPTClient gptClient
    ) {
        return new ChatGptServiceImpl(
                gptClient,
                new ChatGptLocalStorage()
        );
    }

}
