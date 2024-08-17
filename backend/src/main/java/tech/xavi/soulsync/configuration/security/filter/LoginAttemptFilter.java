package tech.xavi.soulsync.configuration.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.controller.ExceptionController;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.ErrorDto;
import tech.xavi.soulsync.entity.LoginAttempt;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.service.configuration.DemoModeService;
import tech.xavi.soulsync.service.user.LoginAttemptService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginAttemptFilter extends OncePerRequestFilter {

    private static final RequestMatcher SIGN_IN_ROUTE = new AntPathRequestMatcher(
            ApiRoutes.EP_ACC_SIGN_IN,
            HttpMethod.POST.name()
    );
    private final LoginAttemptService loginAttemptService;
    private final DemoModeService demoModeService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (demoModeService.isDemoMode()) {
            String ipAddr = request.getRemoteAddr();
            loginAttemptService
                    .checkAttempt(ipAddr);
            LoginAttempt loginAttempt =
                    loginAttemptService.getAttemptByIp(ipAddr);
            if (!loginAttempt.isBlocked())
                filterChain.doFilter(request, response);
            else
                handleTokenFilterException(response, loginAttempt);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void handleTokenFilterException(HttpServletResponse response, LoginAttempt loginAttempt){
        String attemptMessage = loginAttemptService
                .getMaxAttemptsUserMessage(loginAttempt);
        try {
            ExceptionController.handleFilterException(
                    response,
                    objectMapper.writeValueAsString(
                            ErrorDto.builder()
                                    .timestamp(System.currentTimeMillis())
                                    .alertData(AlertData.builder()
                                            .message(String.format(
                                                    SoulSyncError.LOGIN_ATTEMPT_BLOCKED.getMessage(),
                                                    attemptMessage)
                                            ).severity(SoulSyncError.LOGIN_ATTEMPT_BLOCKED.getMessageSeverity())
                                            .build())
                                    .error(SoulSyncError.LOGIN_ATTEMPT_BLOCKED.name())
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
            return !SIGN_IN_ROUTE.matches(request);
        return true;
    }

}
