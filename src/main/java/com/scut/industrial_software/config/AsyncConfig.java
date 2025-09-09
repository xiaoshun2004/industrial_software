package com.scut.industrial_software.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);          // 设置核心线程数
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);        // 设置等待队列的容量为20，这是指在任务被线程执行之前，可以被缓存的最大任务数量。
        executor.setThreadNamePrefix("ExeAsync-");
        executor.initialize();
        return executor;
    }
}
