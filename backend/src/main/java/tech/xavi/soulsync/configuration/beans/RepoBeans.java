package tech.xavi.soulsync.configuration.beans;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.xavi.soulsync.entity.Account;
import tech.xavi.soulsync.repository.KeyValueJsonRepository;

@Configuration
public class RepoBeans {

    @Bean
    public KeyValueJsonRepository<String, JsonNode> kvRepoConfigurationValues(ObjectMapper mapper){
        return new KeyValueJsonRepository<>(mapper,"configuration-data");
    }

    @Bean
    public KeyValueJsonRepository<String, Account> kvRepoAccountValues(ObjectMapper mapper){
        return new KeyValueJsonRepository<>(mapper,"account-data");
    }

}
