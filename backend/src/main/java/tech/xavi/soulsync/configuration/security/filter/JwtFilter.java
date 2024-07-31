package tech.xavi.soulsync.configuration.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.controller.ExceptionController;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.ErrorDto;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.service.user.JwtService;

import java.io.IOException;
import java.util.Optional;

@Order(1)
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) {
        try {
            Optional<String> optToken = jwtService.getTokenFromHeaders(request);
            if (optToken.isPresent()) {
                String token = optToken.get();
                String username = jwtService.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token,userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            jwtService.extractAuthorities(token)
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request,response);
                }
            }
        } catch (io.jsonwebtoken.SignatureException se) {
            handleTokenFilterException(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTokenFilterException(HttpServletResponse response){
        try {
            ExceptionController.handleFilterException(
                    response,
                    objectMapper.writeValueAsString(
                            ErrorDto.builder()
                                    .timestamp(System.currentTimeMillis())
                                    .alertData(AlertData.builder()
                                            .message(SoulSyncError.TOKEN_ERROR.getMessage())
                                            .severity(SoulSyncError.TOKEN_ERROR.getMessageSeverity())
                                            .build())
                                    .error(SoulSyncError.TOKEN_ERROR.name())
                                    .build()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (ApiRoutes.isApiRequest(request))
            return isUnsecuredEndpoint(request);
        return true;
    }

    private boolean isUnsecuredEndpoint(HttpServletRequest request){
        for (RequestMatcher requestMatcher : ApiRoutes.NO_JWT_FILTER_EPS)
            if (requestMatcher.matches(request)) return true;
        return false;
    }

}