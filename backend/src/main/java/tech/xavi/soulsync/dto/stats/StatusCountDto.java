package tech.xavi.soulsync.dto.stats;

import tech.xavi.soulsync.configuration.globals.ProcessStatus;

public record StatusCountDto(
        ProcessStatus status,
        Long total
) {
}
