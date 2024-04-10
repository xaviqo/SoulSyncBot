package tech.xavi.soulsync.repository.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdTokenDto;

import java.util.Map;
import java.util.Objects;

@Component
public class SlskdGateway extends Gateway {

    private final String GET_HEALTH_PATH;
    private final String GET_TOKEN_PATH;
    public SlskdGateway(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.health}") String healthPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.login}") String getTokenPath
    ) {
        super(restTemplate, objectMapper);
        this.GET_HEALTH_PATH = healthPath;
        this.GET_TOKEN_PATH = getTokenPath;
    }

    public SlskdTokenDto getToken(String user, String pass) {
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.POST)
                        .payload(Map.of(
                                "username",user,
                                "password",pass
                        ))
                        .path(GET_TOKEN_PATH)
                        .build(),
                SlskdTokenDto.class
        );
    }

    public boolean isSlskdAlive(){
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .path(GET_HEALTH_PATH)
                        .build(),
                JsonNode.class
        ).asText().equals("true");
    }

    public SlskdGateway baseUrl(String baseUrl){
        if (Objects.nonNull(baseUrl) && !baseUrl.equals(getBaseUrl()))
            setBaseUrl(baseUrl);
        return this;
    }

}
