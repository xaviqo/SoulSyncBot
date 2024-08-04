package tech.xavi.soulsync.repository.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.github.LastReleaseDto;
import tech.xavi.soulsync.exception.SoulSyncException;

@Log4j2
@Component
public class GithubGateway extends Gateway {

    private final String CHECK_RELEASE_PATH;
    
    public GithubGateway(
            RestTemplate restTemplate, 
            ObjectMapper objectMapper,
            @Value("${tech.xavi.soulsync.gateway.base-url.github}") String githubApiBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.github.release}") String checkReleasePath
           ) {
        super(restTemplate, objectMapper, false);
        setBaseUrl(githubApiBaseUrl);
        this.CHECK_RELEASE_PATH = checkReleasePath;
    }

    public LastReleaseDto getLastRelease() {
        try {
            return mappedCall(
                    GatewayRequest.builder()
                            .method(HttpMethod.GET)
                            .path(CHECK_RELEASE_PATH)
                            .build(),
                    LastReleaseDto.class
            );
        } catch (SoulSyncException ex) {
            log.error("Error in call to get version from GitHub repository");
            return LastReleaseDto.builder()
                    .message(HttpStatus.NOT_FOUND.name())
                    .build();
        }
    }

}
