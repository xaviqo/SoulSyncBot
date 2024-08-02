package tech.xavi.soulsync.repository.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.GatewayToken;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadStatusResult;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchRequest;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResult;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdTokenDto;
import tech.xavi.soulsync.entity.db.SlskdRequest;

import java.util.Map;
import java.util.Objects;

@Component
public class SlskdGateway extends Gateway {

    private final String GET_HEALTH_PATH;
    private final String GET_TOKEN_PATH;
    private final String SEARCH_REQUEST_PATH;
    private final String DOWNLOAD_REQUEST_PATH;
    private final String REBOOT_APP_PATH;

    public SlskdGateway(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.health}") String healthPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.login}") String getTokenPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.search}") String searchPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.downloads}") String downloadPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.reboot}") String rebootPath
    ) {
        super(restTemplate, objectMapper, true);
        this.GET_HEALTH_PATH = healthPath;
        this.GET_TOKEN_PATH = getTokenPath;
        this.SEARCH_REQUEST_PATH = searchPath;
        this.DOWNLOAD_REQUEST_PATH = downloadPath;
        this.REBOOT_APP_PATH = rebootPath;
    }

    public void search(
            GatewayToken token,
            SlskdSearchRequest searchRequest
    ) {
        call(
                GatewayRequest.builder()
                        .method(HttpMethod.POST)
                        .token(token.token())
                        .path(SEARCH_REQUEST_PATH)
                        .payload(searchRequest)
                        .build()
        );
    }

    public void deleteSearch(
            GatewayToken token,
            SlskdRequest slskdRequest
    ) {
        call(
                GatewayRequest.builder()
                        .method(HttpMethod.DELETE)
                        .token(token.token())
                        .path(String.format(
                                SEARCH_REQUEST_PATH+"/%s",
                                slskdRequest.getSearchId()
                        ))
                        .build()
        );
    }

    public void clearDownloads(GatewayToken token){
        call(
                GatewayRequest.builder()
                        .method(HttpMethod.DELETE)
                        .token(token.token())
                        .path(DOWNLOAD_REQUEST_PATH+"/all/completed")
                        .build()
        );
    }

    public void sendDownload(
            GatewayToken token,
            SlskdRequest slskdRequest
    ) {
        call(
          GatewayRequest.builder()
                  .method(HttpMethod.POST)
                  .token(token.token())
                  .path(String.format(
                          DOWNLOAD_REQUEST_PATH+"/%s",
                          slskdRequest.getSharedBy()
                  ))
                  .payload(new Map[]{Map.of(
                          "filename",slskdRequest.getFilename(),
                          "size",slskdRequest.getSize())
                  })
                  .build()
        );
    }

    public SlskdSearchResult getSearchStatus(
            GatewayToken token,
            String searchId
    ){
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(String.format(
                                SEARCH_REQUEST_PATH+"/%s",
                                searchId
                        ))
                        .build(),
                SlskdSearchResult.class
        );
    }

    public SlskdDownloadStatusResult[] getDownloadsStatus(GatewayToken token) {
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(DOWNLOAD_REQUEST_PATH)
                        .build(),
                SlskdDownloadStatusResult[].class
        );
    }

    public SlskdSearchResult getSearchResults(
            GatewayToken token,
            String searchId
    ) {
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(String.format(
                                SEARCH_REQUEST_PATH+"/%s",
                                searchId
                        ))
                        .query(Map.of("includeResponses","true"))
                        .build(),
                SlskdSearchResult.class
        );
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

    public void rebootSlskd(GatewayToken token) {
        call(
                GatewayRequest.builder()
                        .method(HttpMethod.PUT)
                        .token(token.token())
                        .path(REBOOT_APP_PATH)
                        .build()
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
