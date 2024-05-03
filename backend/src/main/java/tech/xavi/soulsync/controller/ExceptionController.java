package tech.xavi.soulsync.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.ErrorDto;
import tech.xavi.soulsync.exception.SoulSyncException;

import java.io.IOException;

@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(SoulSyncException.class)
    public ResponseEntity<ErrorDto> handleApiException(
            SoulSyncException exception, HttpServletRequest request
    ) {
        return new ResponseEntity<>(
                ErrorDto.builder()
                        .timestamp(System.currentTimeMillis())
                        .error(exception.getError().name())
                        .alertData(AlertData.builder()
                                .message(exception.getUserMessage())
                                .severity(exception.getError().getMessageSeverity())
                                .build())
                        .build(),
                exception.getHttpStatus()
        );
    }

    public static void handleTokenException(HttpServletResponse response, String jsonError) throws IOException {
        if (!response.isCommitted()) {
            response.addHeader("Content-Type", "application/json");
            response.getWriter().println(jsonError);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
