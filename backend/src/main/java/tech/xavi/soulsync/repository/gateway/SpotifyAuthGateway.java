package tech.xavi.soulsync.repository.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyTokenDto;

@Component
public class SpotifyAuthGateway extends Gateway {

    private final String GET_TOKEN_PATH;
    public SpotifyAuthGateway(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.auth}") String authBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.auth.get-token.ep}") String authEpGetToken
            ) {
        super(restTemplate, objectMapper);
        setBaseUrl(authBaseUrl);
        this.GET_TOKEN_PATH = authEpGetToken;
    }

    public SpotifyTokenDto getToken(String b64credentials) {
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.POST)
                        .path(GET_TOKEN_PATH)
                        .basicCredentials(b64credentials)
                        .payload(new LinkedMultiValueMap<>() {{
                            add("grant_type", "client_credentials");
                        }})
                        .build(),
                SpotifyTokenDto.class
        );
    }
}