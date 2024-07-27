package tech.xavi.soulsync.configuration.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.controller.ExceptionController;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.ErrorDto;
import tech.xavi.soulsync.entity.Role;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.service.user.JwtService;

import java.io.IOException;

@Order(2)
@Component
@RequiredArgsConstructor
public class DemoFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            jwtService
                    .getTokenFromHeaders(request)
                    .ifPresent( token -> {
                        String role = jwtService.extractRole(token);
                        if (Role.DEMO.getWithPrefix().equals(role)) {
                            handleDemoUserAccessNotAllowed(response);
                        } else {
                            try {
                                filterChain.doFilter(request,response);

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (ServletException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleDemoUserAccessNotAllowed(HttpServletResponse response){
        try {
            ExceptionController.handleFilterException(
                    response,
                    objectMapper.writeValueAsString(
                            ErrorDto.builder()
                                    .timestamp(System.currentTimeMillis())
                                    .alertData(AlertData.builder()
                                            .message(SoulSyncError.DEMO_USER_NOT_ALLOWED.getMessage())
                                            .severity(SoulSyncError.DEMO_USER_NOT_ALLOWED.getMessageSeverity())
                                            .build())
                                    .error(SoulSyncError.DEMO_USER_NOT_ALLOWED.name())
                                    .build()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (ApiRoutes.isApiRequest(request))
            return shouldCheckDemoUser(request);
        return true;
    }

    private boolean shouldCheckDemoUser(HttpServletRequest request) {
        for (RequestMatcher requestMatcher : ApiRoutes.NO_AVAILABLE_FOR_DEMO_USER)
            if (requestMatcher.matches(request)) return false;
        return true;
    }
}
