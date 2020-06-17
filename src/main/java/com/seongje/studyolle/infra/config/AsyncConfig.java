package com.seongje.studyolle.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    // 태스크 비동기 처리 - 스레드 풀 //

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processor = Runtime.getRuntime().availableProcessors();

        executor.setCorePoolSize(processor);
        executor.setMaxPoolSize(processor * 2);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        log.error("Error message : 'Async exception'");
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
