package tech.xavi.soulsync.configuration.beans;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.dto.gateway.GatewayToken;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GatewayBeans {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Bean
    public Map<GatewayName, GatewayToken> gatewayTokenMap(){
        return HashMap.newHashMap(2);
    }
}
