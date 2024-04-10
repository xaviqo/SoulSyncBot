package tech.xavi.soulsync.dto.shared;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AlertData {
    private final String message;
    private final MessageSeverity severity;
}
