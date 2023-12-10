package tech.xavi.soulsync.dto.gateway;

import com.mashape.unirest.http.HttpMethod;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class GatewayRequest {
    private final HttpMethod method;
    private final String url;
    private final String token;
    private final String basicCredentials;
    private final Object payload;
    private final Map<String,Object> queryStrings;
    private final Map<String,String> routeParams;
    @Builder.Default
    private final boolean appJson = true;

}