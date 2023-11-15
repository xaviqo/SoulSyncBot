package tech.xavi.soulsync.configuration;

import com.mashape.unirest.http.Unirest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

@Log4j2
@RequiredArgsConstructor
@Configuration
public class HttpClientConfiguration {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper;

    @PostConstruct
    public void configureUnirestObjectMapper() {
        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {

            public String writeValue(Object value) {
                try {
                    String json = mapper.writeValueAsString(value);
                    log.debug("[writeValue] - Json: {}",json);
                    return json;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return mapper.readValue(value, valueType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
