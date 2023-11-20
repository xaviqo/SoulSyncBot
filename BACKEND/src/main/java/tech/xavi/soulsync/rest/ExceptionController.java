package tech.xavi.soulsync.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.dto.rest.ApiErrorPayload;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = SoulSyncException.class)
    public ResponseEntity<ApiErrorPayload> handleException(
            SoulSyncException soulSyncException, HttpServletRequest request
    ){
        return new ResponseEntity<>(
                ApiErrorPayload.builder()
                        .message(soulSyncException.getSoulSyncError().getMessage())
                        .error(soulSyncException.getSoulSyncError().name())
                        .code(soulSyncException.getSoulSyncError().getCode())
                        .moment(System.currentTimeMillis())
                        .build(),
                soulSyncException.getHttpStatus()
        );
    }

}
