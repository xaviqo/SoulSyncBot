package tech.xavi.soulsync.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.dto.gateway.slskd.*;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

@Log4j2
@Component
public class SlskdGateway extends Gateway {

    private final SoulSyncConfiguration.Api apiConfiguration;
    private final String GET_SESSION_URL;
    private final String INIT_SEARCH_URL;
    private final String INIT_DOWNLOAD_URL;
    private final String GET_DOWNLOADS_STATUS_URL;
    private final String CHECK_SEARCH_STATUS_URL;
    private final String DELETE_SEARCH_URL;
    private final String DELETE_DOWNLOAD_URL;
    private final String GET_RESPONSES_URL;
    private final String HEALTH_CHECK_URL;
    private final ObjectMapper objectMapper;


    public SlskdGateway(
            @Value("${tech.xavi.soulsync.gateway.path.slskd.login}") String epSession,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.search}") String search,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.responses}") String responsesPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.download}") String initDownload,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.health}") String healthCheck,
            ObjectMapper objectMapper,
            ConfigurationService configurationService
    ) {
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
        this.objectMapper = objectMapper;
        this.apiConfiguration = configurationService.getConfiguration().api();
    }

    public void download(SlskdDownloadRequest downloadRequest, String token){
        try {
            String formattedUrl = String.format(INIT_DOWNLOAD_URL,downloadRequest.username());

            log.debug("[download] - URI: {}",formattedUrl);
            log.debug("[download] - Payload: {}, {}",downloadRequest,token);

            HttpResponse<String> response = Unirest.post(buildUrl(formattedUrl))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .body(new SlskdDownloadPayload[]{downloadRequest.payload()})
                    .asString();

            int responseStatus = response.getStatus();
            log.debug("[download] - Response status code: {}",responseStatus);
            log.debug("[download] - Complete response: {}", response);


        } catch (Exception e) {
            log.error("[download] - Error while making the request: {}",e.getMessage());
            e.printStackTrace();
        }
    }

    public SlskdSearchResult[] getSearchResults(String searchId, String token){
        try {
            String formattedUrl = String.format(GET_RESPONSES_URL,searchId);

            log.debug("[getSearchResults] - URI: {}",formattedUrl);
            log.debug("[getSearchResults] - Payload: {}, {}",searchId,token);

            HttpResponse<JsonNode> response = Unirest.get(buildUrl(formattedUrl))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getSearchResults] - Response status code: {}",responseStatus);

            String responseJson = response.getBody().toString();
            SlskdSearchResult[] searchResults = objectMapper.readValue(responseJson, SlskdSearchResult[].class);
            log.debug("[getSearchResults] - Total Results: {}",searchResults.length);

            return searchResults;
        } catch (Exception e) {
            log.error("[getSearchResults] - Error while making the request: {}",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public SlskdSearchStatus checkSearchStatus(String searchId, String token){
        try {
            String formattedUrl = String.format(CHECK_SEARCH_STATUS_URL,searchId);

            log.debug("[checkSearchStatus] - URI: {}",formattedUrl);
            log.debug("[checkSearchStatus] - Payload: {}, {}",searchId,token);

            HttpResponse<JsonNode> response = Unirest.get(buildUrl(formattedUrl))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[checkSearchStatus] - Response status code: {}",responseStatus);

            if (response.getStatus() > 399)
                return null;

            String responseJson = response.getBody().toString();
            log.debug("[checkSearchStatus] - Complete response: {}",responseJson);

            return objectMapper.readValue(responseJson, SlskdSearchStatus.class);
        } catch (Exception e) {
            log.error("[checkSearchStatus] - Error while making the request: {}",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public SlskdSearchStatus initSearch(SlskdSearchQuery songReq, String token){
        try {
            log.debug("[initSearch] - URI: {}",INIT_SEARCH_URL);
            log.debug("[initSearch] - Payload: {}, {}",songReq,token);


            HttpResponse<JsonNode> response = Unirest.post(buildUrl(INIT_SEARCH_URL))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .body(songReq)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[initSearch] - Response status code: {}",responseStatus);

            String responseJson = response.getBody().toString();
            log.debug("[initSearch] - Complete response: {}",responseJson);

            return objectMapper.readValue(responseJson, SlskdSearchStatus.class);
        } catch (Exception e) {
            log.error("[initSearch] - Error while making the request: {}",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public SlskdDownloadStatus[] getDownloadsStatus(String token){
        try {
            log.debug("[getDownloadsStatus] - URI: {}",GET_DOWNLOADS_STATUS_URL);
            log.debug("[getDownloadsStatus]- Payload: {}",token);

            HttpResponse<JsonNode> response = Unirest.get(buildUrl(GET_DOWNLOADS_STATUS_URL))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getDownloadsStatus] - Response status code: {}", responseStatus);

            return objectMapper.readValue(response.getBody().toString(), SlskdDownloadStatus[].class);
        } catch (Exception e) {
            log.error("[getDownloadsStatus] - Error getting Slskd downloads status: " + e.getMessage());
            return new SlskdDownloadStatus[]{};
        }
    }

    public void deleteDownload(String token, String user, String searchId){
        String formattedUrl = String.format(DELETE_DOWNLOAD_URL,user,searchId);
        try {
            log.debug("[deleteDownload] - URI: {}",formattedUrl);
            log.debug("[deleteDownload]- Payload: {}, {}",searchId,token);

            HttpResponse<String> response = Unirest.delete(buildUrl(formattedUrl))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .asString();

            log.debug("[deleteDownload] - Response status code: {}", response.getStatus());
        } catch (Exception e) {
            log.error("[deleteDownload] - Error deleting download: " + e.getMessage());
        }
    }

    public void deleteSearch(String searchId, String token){
        String formattedUrl = String.format(DELETE_SEARCH_URL,searchId);
        try {
            log.debug("[deleteSearch] - URI: {}",formattedUrl);
            log.debug("[deleteSearch]- Payload: {}, {}",searchId,token);

            HttpResponse<String> response = Unirest.delete(buildUrl(formattedUrl))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .asString();

            log.debug("[deleteSearch] - Response status code: {}", response.getStatus());
        } catch (Exception e) {
            log.error("[deleteSearch] - Error deleting search: " + e.getMessage());
        }
    }

    public boolean isSlskdApiHealthy(){
        final String healthyResponse = "Healthy";
        try {
            log.debug("[healthCheck] - URI: {}",HEALTH_CHECK_URL);

            HttpResponse<String> response = Unirest.get(buildUrl(HEALTH_CHECK_URL))
                    .asString();

            log.debug("[healthCheck] - Response status code: {}", response.getStatus());
            log.debug("[healthCheck] - Complete response: {}", response.getBody());

            return response.getBody().equals(healthyResponse);
        } catch (Exception e) {
            log.error("[healthCheck] - Unable to connect to the local Slskd API: " + e.getMessage());
            return false;
        }
    }

    public SlskdTokenRes getToken(String username, String password){
        try {
            log.debug("[getToken] - URI: {}",GET_SESSION_URL);
            log.debug("[getToken]- Payload: {}, {}",username,password);

            HttpResponse<JsonNode> response = Unirest.post(buildUrl(GET_SESSION_URL))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .body(SlskdLogin.builder()
                            .username(username)
                            .password(password)
                            .build())
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getToken] - Response status code: {}", responseStatus);

            String responseJson = response.getBody().toString();
            log.debug("[getToken] - Complete response: {}", responseJson);

            return objectMapper.readValue(responseJson, SlskdTokenRes.class);
        } catch (Exception e) {
            log.error("[getToken] - Error while getting the token: " + e.getMessage());
            return null;
        }
    }

    private String buildUrl(String endpoint){
        return apiConfiguration.getSlskdUrl()+endpoint;
    }
}
