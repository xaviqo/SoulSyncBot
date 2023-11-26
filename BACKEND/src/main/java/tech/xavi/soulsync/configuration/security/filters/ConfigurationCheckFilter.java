package tech.xavi.soulsync.configuration.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.xavi.soulsync.configuration.constants.ConfigurationFinals;
import tech.xavi.soulsync.dto.rest.ApiErrorPayload;
import tech.xavi.soulsync.entity.SoulSyncError;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ConfigurationCheckFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final boolean disabled = true;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (disabled || ConfigurationService.isSoulSyncConfigured()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ApiErrorPayload.builder()
                                    .message(SoulSyncError.CONFIGURATION_EXPECTED.getMessage())
                                    .error(SoulSyncError.CONFIGURATION_EXPECTED.name())
                                    .code(SoulSyncError.CONFIGURATION_EXPECTED.getCode())
                                    .moment(System.currentTimeMillis())
                                    .build()
                    )
            );
            response.getWriter().flush();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        for (RequestMatcher matcher : ConfigurationFinals.UNFILTERED_CFG_FILTER_ROUTES)
            if (matcher.matches(request)) return true;
        return false;
    }


}
