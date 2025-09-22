package com.ai.imageagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class AsyncExecutorsConfig {

    @Bean(name = "vueBuilderExecutor")
    public ThreadPoolExecutor vueBuilderExecutor() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        int max = Math.max(4, Runtime.getRuntime().availableProcessors());
        return new ThreadPoolExecutor(
                cores,
                max,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(64),
                r -> new Thread(r, "vue-builder-" + System.currentTimeMillis()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Bean(name = "saveCoverExecutor")
    public ThreadPoolExecutor saveCoverExecutor() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        int max = Math.max(4, Runtime.getRuntime().availableProcessors());
        return new ThreadPoolExecutor(
                cores,
                max,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(64),
                r -> new Thread(r, "save-cover-" + System.currentTimeMillis()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Bean(name = "agentModeExecutor")
    public ThreadPoolExecutor agentModeExecutor() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        int max = Math.max(4, Runtime.getRuntime().availableProcessors());
        return new ThreadPoolExecutor(
                cores,
                max,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(64),
                r -> new Thread(r, "agent-mode-" + System.currentTimeMillis()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}


