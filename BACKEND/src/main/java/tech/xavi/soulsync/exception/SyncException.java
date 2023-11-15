package tech.xavi.soulsync.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@RequiredArgsConstructor
public class SyncException extends RuntimeException{
    private final SyncError syncError;
    private final HttpStatus httpStatus;
}
