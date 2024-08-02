package tech.xavi.soulsync.dto.stats;

import lombok.Builder;

@Builder
public record ProcessSummaryDto(
        long totalProcessed,
        long totalSuccess,
        long totalFailed,
        String lastSuccess,
        String lastFailed,
        String runningTime
) {
}
