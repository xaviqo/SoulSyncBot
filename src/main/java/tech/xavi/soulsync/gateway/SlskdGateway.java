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
import tech.xavi.soulsync.dto.gateway.*;

@Log4j2
@Component
public class SlskdGateway extends Gateway {

    private final String GET_SESSION_URL;
    private final String INIT_SEARCH_URL;
    private final String INIT_DOWNLOAD_URL;
    private final String CHECK_SEARCH_STATUS_URL;
    private final String DELETE_SEARCH_URL;
    private final String GET_RESPONSES_URL;
    private final String HEALTH_CHECK_URL;
    private final ObjectMapper objectMapper;


    public SlskdGateway(
            @Value("${tech.xavi.soulsync.gateway.base-url.slskd.main}") String baseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.login}") String epSession,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.search}") String search,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.responses}") String responsesPath,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.download}") String initDownload,
            @Value("${tech.xavi.soulsync.gateway.path.slskd.health}") String healthCheck,
            ObjectMapper objectMapper
    ) {
        final String addParam = "/%s";
        this.GET_SESSION_URL = baseUrl+epSession;
        this.INIT_SEARCH_URL = baseUrl+search;
        this.CHECK_SEARCH_STATUS_URL = baseUrl+search+addParam;
        this.DELETE_SEARCH_URL = baseUrl+search+addParam;
        this.GET_RESPONSES_URL = baseUrl+search+addParam+responsesPath;
        this.INIT_DOWNLOAD_URL = baseUrl+initDownload+addParam;
        this.HEALTH_CHECK_URL = baseUrl+healthCheck;
        this.objectMapper = objectMapper;
    }

    public void download(SlskdDownloadRequest downloadRequest, String token){
        try {
            String formattedUrl = String.format(INIT_DOWNLOAD_URL,downloadRequest.username());

            log.debug("[download] - URI: {}",formattedUrl);
            log.debug("[download] - Payload: {}, {}",downloadRequest,token);

            HttpResponse<String> response = Unirest.post(formattedUrl)
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

            HttpResponse<JsonNode> response = Unirest.get(formattedUrl)
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

            HttpResponse<JsonNode> response = Unirest.get(formattedUrl)
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


            HttpResponse<JsonNode> response = Unirest.post(INIT_SEARCH_URL)
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

    public SlskdTokenRes getToken(String username, String password){
        try {
            log.debug("[getToken] - URI: {}",GET_SESSION_URL);
            log.debug("[getToken]- Payload: {}, {}",username,password);

            HttpResponse<JsonNode> response = Unirest.post(GET_SESSION_URL)
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

    public void deleteSearch(String searchId, String token){
        String formattedUrl = String.format(DELETE_SEARCH_URL,searchId);
        try {
            log.debug("[deleteSearch] - URI: {}",formattedUrl);
            log.debug("[deleteSearch]- Payload: {}, {}",searchId,token);

            HttpResponse<String> response = Unirest.delete(formattedUrl)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .asString();

            int responseStatus = response.getStatus();
            log.debug("[deleteSearch] - Response status code: {}", responseStatus);
            log.debug("[deleteSearch] - Complete response: {}", response.getBody());


        } catch (Exception e) {
            log.error("[deleteSearch] - Error deleting search: " + e.getMessage());
        }
    }

    public boolean healthCheck(){
        final String healthyResponse = "Healthy";

        try {
            log.debug("[healthCheck] - URI: {}",GET_SESSION_URL);

            HttpResponse<String> response = Unirest.get(HEALTH_CHECK_URL)
                    .asString();

            log.debug("[healthCheck] - Response status code: {}", response.getStatus());
            log.debug("[healthCheck] - Complete response: {}", response.getBody());

            return response.getBody().equals(healthyResponse);
        } catch (Exception e) {
            log.error("[healthCheck] - Unable to connect to the local Slskd API: " + e.getMessage());
            return false;
        }
    }
}
