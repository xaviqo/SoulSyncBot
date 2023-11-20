package tech.xavi.soulsync.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.entity.SoulSyncError;

import java.util.Objects;

@Log4j2
public class Gateway {
    public final String BASIC_PREFIX = "Basic ";
    public final String BEARER_PREFIX = "Bearer ";

    public final ObjectMapper objectMapper;

    public Gateway(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <U> U callMapped(ExternalApi api, GatewayRequest request, Class<U> responseClass){
        try {
            return this.objectMapper.readValue(
                    call(api,request),
                    responseClass
            );
        } catch (JSONException | JsonProcessingException e) {
            handleJsonProcessingException(api, request, e);
        }
        return null;
    }

    public String call(ExternalApi api, GatewayRequest request) {
        try {
            HttpRequestWithBody unirestRequest = createUnirestRequest(api,request);
            return getResponse(api,unirestRequest);
        } catch (UnirestException unirestException) {
            handleUnirestException(api, request, unirestException);
        } catch (JSONException jsonException) {
            handleJsonProcessingException(api, request, jsonException);
        } catch (Exception exception) {
            handleGenericException(api, exception);
        }
        return null;
    }

    private String getResponse(ExternalApi api, HttpRequestWithBody unirestRequest) throws UnirestException {
        HttpResponse<String> response = unirestRequest.asString();
        int responseStatus = response.getStatus();
        String responseBody = response.getBody();
        log.debug("{} Response status code: {}", api.getLoggerMethodPrefix(), responseStatus);
        log.debug("{} Complete response: {}", api.getLoggerMethodPrefix(), responseBody);
        return responseBody;
    }

    private HttpRequestWithBody createUnirestRequest(ExternalApi api, GatewayRequest request) {
        log.debug("{} URI: {}", api.getLoggerMethodPrefix(), request.getUrl());

        HttpRequestWithBody unirestRequest = new HttpRequestWithBody(request.getMethod(), request.getUrl());
        if (request.isAppJson()) unirestRequest.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        handleAuthorizationHeaders(unirestRequest, request);
        handlePayload(unirestRequest, api, request);
        handleQueryStrings(unirestRequest, request);
        handleRouteParams(unirestRequest, request);

        return unirestRequest;
    }

    private void handleAuthorizationHeaders(HttpRequestWithBody unirestRequest, GatewayRequest request) {
        if (Objects.nonNull(request.getToken())) {
            unirestRequest.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + request.getToken());
        } else if (Objects.nonNull(request.getBasicCredentials())) {
            unirestRequest.field("grant_type", "client_credentials");
            unirestRequest.header(HttpHeaders.AUTHORIZATION, BASIC_PREFIX + request.getBasicCredentials());
        }
    }

    private void handlePayload(HttpRequestWithBody unirestRequest, ExternalApi api, GatewayRequest request) {
        if (Objects.nonNull(request.getPayload())) {
            log.debug("{} Payload: {}", api.getLoggerMethodPrefix(), request.getPayload().toString());
            unirestRequest.body(request.getPayload());
        }
    }

    private void handleQueryStrings(HttpRequestWithBody unirestRequest, GatewayRequest request) {
        if (Objects.nonNull(request.getQueryStrings())) {
            unirestRequest.queryString(request.getQueryStrings());
        }
    }

    private void handleRouteParams(HttpRequestWithBody unirestRequest, GatewayRequest request) {
        if (Objects.nonNull(request.getRouteParams())) {
            String key = request.getRouteParams().getKey();
            String val = request.getRouteParams().getValue();
            unirestRequest.routeParam(key, val);
        }
    }

    private void handleUnirestException(ExternalApi api, GatewayRequest request, UnirestException exception) {
        SoulSyncError soulSyncError = SoulSyncError.GATEWAY_CALL_ERROR.buildMessage(api.name());
        log.error("{} (UnirestException) URL: {} - {} - Message: {}",
                api.getLoggerMethodPrefix(), request.getUrl(), soulSyncError.getMessage(), exception.getMessage());
        exception.printStackTrace();
        throw new SoulSyncException(soulSyncError, HttpStatus.BAD_REQUEST);
    }

    private void handleJsonProcessingException(ExternalApi api, GatewayRequest request, Exception exception) {
        SoulSyncError soulSyncError = SoulSyncError.GATEWAY_JSON_DESERIALIZATION_ERROR.buildMessage(api.name());
        log.error("{} (JsonProcessingException) URL: {} - {} - Message: {}",
                api.getLoggerMethodPrefix(), request.getUrl(), soulSyncError.getMessage(), exception.getMessage());
        exception.printStackTrace();
        throw new SoulSyncException(soulSyncError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void handleGenericException(ExternalApi api, Exception exception) {
        log.error("{} Error while making the request: {}", api.getLoggerMethodPrefix(), exception.getMessage());
        exception.printStackTrace();
        throw new SoulSyncException(
                SoulSyncError.GATEWAY_FATAL_ERROR.buildMessage(""),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @RequiredArgsConstructor
    public enum ExternalApi {
        SLSKD,
        SPOTIFY;
        private String method;

        public ExternalApi method(String method){
            this.method = method;
            return this;
        }

        public String getLoggerMethodPrefix(){
            return String.format(
                    "[ %s | %s ] -",
                    this.name(),
                    this.method
            );
        }

    }


}
