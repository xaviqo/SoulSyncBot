package tech.xavi.soulsync.configuration.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.xavi.soulsync.configuration.constants.ConfigurationFinals;
import tech.xavi.soulsync.dto.rest.ApiErrorPayload;
import tech.xavi.soulsync.entity.SoulSyncError;
import tech.xavi.soulsync.service.auth.JwtService;

import java.io.IOException;
import java.util.Optional;

import static tech.xavi.soulsync.configuration.constants.ConfigurationFinals.TOKEN_PREFIX;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {
        Optional<String> accessToken = getAccessToken(request);
        if (accessToken.isPresent()) {
            String userEmail = jwtService.extractUsername(accessToken.get());
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(accessToken.get(), userDetails)) {
                filterChain.doFilter(request, response);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ApiErrorPayload.builder()
                                    .message(SoulSyncError.TOKEN_NOT_VALID.getMessage())
                                    .error(SoulSyncError.TOKEN_NOT_VALID.name())
                                    .code(SoulSyncError.TOKEN_NOT_VALID.getCode())
                                    .moment(System.currentTimeMillis())
                                    .build()
                    )
            );
            response.getWriter().flush();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        for (RequestMatcher matcher : ConfigurationFinals.UNFILTERED_JWT_FILTER_ROUTES)
            if (matcher.matches(request)) return true;
        return false;
    }

    public Optional<String> getAccessToken(HttpServletRequest request){
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return Optional.of(authHeader.replace(TOKEN_PREFIX, ""));
        }
        return Optional.empty();
    }
}
