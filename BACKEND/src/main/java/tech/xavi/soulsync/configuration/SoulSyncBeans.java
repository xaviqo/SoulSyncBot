package tech.xavi.soulsync.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@RequiredArgsConstructor
@EnableAsync
@Configuration
public class SoulSyncBeans {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    //@Bean(name = "asyncExecutor")
    //public Executor asyncExecutor(){
    //    int poolSize = configurationService.getConfiguration().app().getMaxSongsDownloadingAtTime();
    //    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    //    executor.setCorePoolSize(poolSize);
    //    executor.setMaxPoolSize(poolSize);
    //    executor.setQueueCapacity(100);
    //    executor.setThreadNamePrefix("AsyncThread-");
    //    executor.initialize();
    //    return executor;
    //}

}
