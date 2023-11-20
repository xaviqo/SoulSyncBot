package tech.xavi.soulsync.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.xavi.soulsync.exception.ApiErrorPayload;
import tech.xavi.soulsync.exception.SyncException;

@RestControllerAdvice

public class ExceptionController {

    @ExceptionHandler(value = SyncException.class)
    public ResponseEntity<ApiErrorPayload> handleException(
            SyncException syncException, HttpServletRequest request
    ){
        return new ResponseEntity<>(
                ApiErrorPayload.builder()
                        .path(request.getRequestURI())
                        .method(request.getMethod())
                        .message(syncException.getSyncError().getMessage())
                        .error(syncException.getSyncError().name())
                        .code(syncException.getSyncError().getCode())
                        .moment(System.currentTimeMillis())
                        .status(syncException.getHttpStatus())
                        .build(),
                syncException.getHttpStatus()
        );
    }
}
