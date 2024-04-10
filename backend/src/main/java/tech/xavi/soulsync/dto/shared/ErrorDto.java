package tech.xavi.soulsync.dto.shared;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder @Getter
public class ErrorDto {
    private String error;
    private long timestamp;
    private AlertData alertData;
}
