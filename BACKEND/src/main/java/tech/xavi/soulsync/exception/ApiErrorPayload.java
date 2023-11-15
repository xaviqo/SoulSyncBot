package tech.xavi.soulsync.exception;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ApiErrorPayload(
        String path,
        String method,
        String message,
        String error,
        int code,
        long moment,
        HttpStatus status
) {
}
