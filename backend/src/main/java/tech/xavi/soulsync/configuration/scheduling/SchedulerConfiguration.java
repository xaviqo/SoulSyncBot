package tech.xavi.soulsync.configuration.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfiguration {

    public static final String SOULSYNC_TASK_POOL_NAME = "SS-SCHEDULER";
    public static final String SOULSYNC_SCHEDULER_POOL_NAME = "SS-SCHEDULER";

    @Bean(name = SOULSYNC_TASK_POOL_NAME)
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }

    @Bean(name = SOULSYNC_SCHEDULER_POOL_NAME)
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix(SOULSYNC_SCHEDULER_POOL_NAME);
        taskScheduler.initialize();
        return taskScheduler;
    }
}