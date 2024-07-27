package tech.xavi.soulsync.configuration.security.filter;

import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.entity.Role;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.service.user.JwtService;

import java.util.Set;

@ControllerAdvice @RequiredArgsConstructor
public class APIConfigurationResponseFilter implements ResponseBodyAdvice<Object> {

    private final JwtService jwtService;
    private static final RequestMatcher EP_FIELDS = new AntPathRequestMatcher(
            ApiRoutes.EP_FIELDS, HttpMethod.GET.name()
    );

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (EP_FIELDS.matches(((ServletServerHttpRequest) request).getServletRequest())) {
            if (body instanceof Set) {
                String tkn = getToken(request);
                String role = jwtService.extractRole(tkn);
                if (Role.DEMO.getWithPrefix().equals(role)) {
                    Set<ConfigurationField> fields = (Set<ConfigurationField>) body;
                    fields.forEach( f -> {
                        if (f.getSection().equals(ConfigurationField.Section.API)){
                            f.setValue(TextNode.valueOf("************"));
                        }
                    } );
                    return fields;
                }
            }
        }
        return body;
    }

    private String getToken(ServerHttpRequest request) {
        try {
            return request
                    .getHeaders()
                    .get(HttpHeaders.AUTHORIZATION)
                    .get(0)
                    .replace(JwtService.TOKEN_PREFIX,"");
        } catch (Exception e){
            return "";
        }
    }
}