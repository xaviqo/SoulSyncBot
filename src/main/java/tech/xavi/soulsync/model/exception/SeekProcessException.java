package tech.xavi.soulsync.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeekProcessException extends RuntimeException{
    private final SeekProcessError seekProcessError;
}
