package tech.xavi.soulsync.repository.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.exception.GatewayException;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;

import java.util.Objects;

@Log4j2
@RequiredArgsConstructor
public abstract class Gateway {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Getter private final boolean shouldAddJsonContentTypeHeader;
    @Setter @Getter
    private String baseUrl;

    public <U> U mappedCall(GatewayRequest request, Class<U> clazz) {
        try {
            return objectMapper.readValue(
                    call(request),
                    clazz
            );
        } catch (Exception exception) {
            SoulSyncException soulSyncException = new SoulSyncException(
                    SoulSyncError.GATEWAY_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Object[]{
                            exception.getClass().getSimpleName(),
                            clazz.getSimpleName(),
                            getBaseUrl()+request.path()
                    }
            );
            log.error(soulSyncException.getUserMessage(),exception);
            throw soulSyncException;
        }
    }

    public String call(GatewayRequest request) {
        try {
            return restTemplate.exchange(
                    getRequestURI(request),
                    request.method(),
                    new HttpEntity<>(
                            request.payload(),
                            createHttpHeaders(request)),
                    String.class
            ).getBody();
        } catch (HttpServerErrorException e) {
            log.warn("API call failed - Status Code: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GatewayException("Remote service error: " + e.getResponseBodyAsString());
        } catch (HttpClientErrorException e) {
            log.warn("Client error - Status Code: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GatewayException("Client error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error occurred during API call: {}", e.getMessage());
            throw new GatewayException("Unexpected service error", e);
        }
    }


    private HttpHeaders createHttpHeaders(GatewayRequest request) {
        final String BEARER_PREFIX = "Bearer ";
        final String BASIC_PREFIX = "Basic ";
        final HttpHeaders headers = new HttpHeaders();

        headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);

        if (isShouldAddJsonContentTypeHeader())
            headers.setContentType(MediaType.APPLICATION_JSON);

        if (Objects.nonNull(request.token()))
            headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + request.token());
        else if (Objects.nonNull(request.basicCredentials()))
            headers.set(HttpHeaders.AUTHORIZATION, BASIC_PREFIX + request.basicCredentials());

        return headers;
    }

    private String getRequestURI(GatewayRequest request){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(getBaseUrl() + request.path());
        handleQueryStrings(uriBuilder,request);
        handleRouteParams(uriBuilder,request);
        return uriBuilder.toUriString();
    }

    private void handleQueryStrings(UriComponentsBuilder uriBuilder, GatewayRequest request) {
        if (Objects.nonNull(request.query()))
            request.query().forEach(uriBuilder::queryParam);
    }

    private void handleRouteParams(UriComponentsBuilder uriBuilder, GatewayRequest request) {
        if (Objects.nonNull(request.routeParams()))
            request.routeParams().forEach((key, value) -> {
                if (key != null && value != null) {
                    String placeHolder = "{" + key + "}";
                    uriBuilder.replacePath(
                            Objects.requireNonNull(uriBuilder
                                            .build()
                                            .getPath())
                                    .replace(placeHolder, value)
                    );
                }
            });
    }

}
