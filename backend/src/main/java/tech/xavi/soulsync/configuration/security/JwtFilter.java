package tech.xavi.soulsync.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.ErrorDto;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.rest.ExceptionController;
import tech.xavi.soulsync.service.user.JwtService;

import java.io.IOException;

@Log4j2
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
    ) throws IOException
    {
        try {
            jwtService.getTokenFromHeaders(request).ifPresent(token -> {
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
                }
            });
            filterChain.doFilter(request,response);
        } catch (Exception e){
            e.printStackTrace();
            ExceptionController.handleTokenException(
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
        }
    }
}