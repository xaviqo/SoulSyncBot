package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Builder
public record GatewayRequest(
        String baseUrl,
        HttpMethod method,
        String path,
        String token,
        String basicCredentials,
        Object payload,
        Map<String,Object> query,
        Map<String,String> routeParams,
        Map<String,String> requestParams
) {
}
