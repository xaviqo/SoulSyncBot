package tech.xavi.soulsync.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.slskd.*;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

@Log4j2
@Component
public class SlskdGateway extends Gateway {

    private final String GET_SESSION_URL;
    private final String INIT_SEARCH_URL;
    private final String INIT_DOWNLOAD_URL;
    private final String GET_DOWNLOADS_STATUS_URL;
    private final String CHECK_SEARCH_STATUS_URL;
    private final String DELETE_SEARCH_URL;
    private final String DELETE_DOWNLOAD_URL;
    private final String GET_RESPONSES_URL;
    private final String HEALTH_CHECK_URL;

    public SlskdGateway(
            @Value("${tech.xavi.soulsync.gateway.path.slskd.login}") String epSession,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.search}") String search,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.responses}") String responsesPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.download}") String initDownload,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.health}") String healthCheck,
            ObjectMapper objectMapper
    ) {
        super(objectMapper);
        final String addParam = "/%s";
        this.GET_SESSION_URL = epSession;
        this.INIT_SEARCH_URL = search;
        this.CHECK_SEARCH_STATUS_URL = search+addParam;
        this.DELETE_SEARCH_URL = search+addParam;
        this.DELETE_DOWNLOAD_URL = initDownload+addParam+addParam;
        this.GET_RESPONSES_URL = search+addParam+responsesPath;
        this.INIT_DOWNLOAD_URL = initDownload+addParam;
        this.GET_DOWNLOADS_STATUS_URL = initDownload;
        this.HEALTH_CHECK_URL = healthCheck;
    }

    public void download(SlskdDownloadRequest downloadRequest, String token){
        final String formattedUrl = String.format(INIT_DOWNLOAD_URL,downloadRequest.username());
        call(
            ExternalApi.SLSKD.method("download"),
            GatewayRequest.builder()
                    .method(HttpMethod.POST)
                    .url(buildUrl(formattedUrl))
                    .token(token)
                    .payload(new SlskdDownloadPayload[]{downloadRequest.payload()})
                    .build()
        );
    }

    public SlskdSearchResult[] getSearchResults(String searchId, String token){
        final String formattedUrl = String.format(GET_RESPONSES_URL,searchId);
        return callMapped(
                ExternalApi.SLSKD.method("getSearchResults"),
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(buildUrl(formattedUrl))
                        .token(token)
                        .build(),
                SlskdSearchResult[].class
        );
    }

    public SlskdSearchStatus checkSearchStatus(String searchId, String token){
        final String formattedUrl = String.format(CHECK_SEARCH_STATUS_URL,searchId);
        return callMapped(
                ExternalApi.SLSKD.method("checkSearchStatus"),
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(buildUrl(formattedUrl))
                        .token(token)
                        .build(),
                SlskdSearchStatus.class
        );
    }

    public SlskdSearchStatus initSearch(SlskdSearchQuery songReq, String token){
        return callMapped(
                ExternalApi.SLSKD.method("initSearch"),
                GatewayRequest.builder()
                        .method(HttpMethod.POST)
                        .url(buildUrl(INIT_SEARCH_URL))
                        .payload(songReq)
                        .token(token)
                        .build(),
                SlskdSearchStatus.class
        );
    }

    public SlskdDownloadStatus[] getDownloadsStatus(String token){
        return callMapped(
                ExternalApi.SLSKD.method("getDownloadsStatus"),
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(buildUrl(GET_DOWNLOADS_STATUS_URL))
                        .token(token)
                        .build(),
                SlskdDownloadStatus[].class
        );
    }

    public void deleteDownload(String token, String user, String searchId){
        final String formattedUrl = String.format(DELETE_DOWNLOAD_URL,user,searchId);
        call(
                ExternalApi.SLSKD.method("deleteDownload"),
                GatewayRequest.builder()
                        .method(HttpMethod.DELETE)
                        .url(buildUrl(formattedUrl))
                        .token(token)
                        .build()
        );
    }

    public void deleteSearch(String searchId, String token){
        final String formattedUrl = String.format(DELETE_SEARCH_URL,searchId);
        call(
                ExternalApi.SLSKD.method("deleteSearch"),
                GatewayRequest.builder()
                        .method(HttpMethod.DELETE)
                        .url(buildUrl(formattedUrl))
                        .token(token)
                        .build()
        );
    }

    public SlskdTokenRes getToken(String username, String password){
        return callMapped(
                ExternalApi.SLSKD.method("getToken"),
                GatewayRequest.builder()
                        .method(HttpMethod.POST)
                        .url(buildUrl(GET_SESSION_URL))
                        .payload(SlskdLogin.builder()
                                .username(username)
                                .password(password)
                                .build())
                        .build(),
                SlskdTokenRes.class
        );
    }

    public boolean isSlskdApiHealthy(){
        final String healthyResponse = "Healthy";
        try {
            log.debug("[healthCheck] - SLSKD - URI: {}",HEALTH_CHECK_URL);
            HttpResponse<String> response = Unirest.get(buildUrl(HEALTH_CHECK_URL)).asString();
            log.debug("[healthCheck] - SLSKD - Response status code: {}", response.getStatus());
            return response.getBody().equals(healthyResponse);
        } catch (Exception e) {
            log.error("[healthCheck] - SLSKD - Unable to connect to the SLSKD API: " + e.getMessage());
            return false;
        }
    }

    private String buildUrl(String endpoint){
        return getConfiguration().getSlskdUrl()+endpoint;
    }

    private SoulSyncConfiguration.Api getConfiguration(){
        return ConfigurationService.instance().cfg().api();
    }
}
