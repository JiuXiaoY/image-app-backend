package com.ai.imageagent.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AiChatModelConfig {

    private static final String apiKey = "sk-01fd3eab37fb4e14b332bc08eae6e2d3";

    private static final String modelName = "deepseek-chat";

    private static final String baseUrl = "https://api.deepseek.com";

    private static final Integer maxTokens = 8192;

    private static final String responseFormat = "json_object";

    @Bean
    @Scope("prototype")
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .logRequests(Boolean.TRUE)
                .logResponses(Boolean.TRUE)
                .maxTokens(maxTokens)
//                .responseFormat(responseFormat)
                .build();
    }

    @Bean
    @Scope("prototype")
    public StreamingChatModel flowChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .logRequests(Boolean.TRUE)
                .logResponses(Boolean.TRUE)
                .maxTokens(maxTokens)
                .build(); // 注意：Streaming 模型一般不需要设置 responseFormat
    }
}
