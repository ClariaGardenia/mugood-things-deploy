package com.muGood.web.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class DashScopeManualConfig {
    @Bean
    @ConditionalOnExpression("'${spring.ai.dashscope.api-key:}' != ''")
    public ChatModel dashScopeChatModel(@Value("${spring.ai.dashscope.api-key}") String apiKey,
                                        @Value("${spring.ai.dashscope.chat.options.model:qwen-vl-plus}") String model,
                                        @Value("${spring.ai.dashscope.chat.options.temperature:0.4}") Double temperature,
                                        ObjectProvider<ToolCallingManager> toolCallingManager,
                                        ObjectProvider<RetryTemplate> retryTemplate,
                                        ObjectProvider<ObservationRegistry> observationRegistry,
                                        ObjectProvider<ToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicate) {
        ObservationRegistry registry = observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP);
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .withStream(true)
                .withIncrementalOutput(true)
                .withMultiModel(true)
                .build();
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .toolCallingManager(toolCallingManager.getIfAvailable(() -> DefaultToolCallingManager.builder()
                        .observationRegistry(registry)
                        .build()))
                .retryTemplate(retryTemplate.getIfAvailable(() -> RetryUtils.DEFAULT_RETRY_TEMPLATE))
                .observationRegistry(registry)
                .toolExecutionEligibilityPredicate(toolExecutionEligibilityPredicate.getIfAvailable(DefaultToolExecutionEligibilityPredicate::new))
                .build();
    }
}
