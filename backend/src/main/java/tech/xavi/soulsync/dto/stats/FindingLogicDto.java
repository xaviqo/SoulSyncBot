package tech.xavi.soulsync.dto.stats;

import lombok.Builder;

@Builder
public record FindingLogicDto(
        long totalFlexible,
        long totalStrict
) {
}
