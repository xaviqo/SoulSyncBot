package tech.xavi.soulsync.configuration.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import tech.xavi.soulsync.entity.sub.SoulSyncError;

@Getter
@Setter
@RequiredArgsConstructor
public class SoulSyncException extends RuntimeException{
    private final SoulSyncError soulSyncError;
    private final HttpStatus httpStatus;
}
