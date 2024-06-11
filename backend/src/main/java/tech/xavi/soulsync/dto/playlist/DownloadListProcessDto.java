package tech.xavi.soulsync.dto.playlist;

import lombok.Builder;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;

@Builder
public record DownloadListProcessDto(
        long id,
        String policyId,
        boolean isActive,
        DownloadPriority priority,
        long lastCheck,
        long attempts,
        long totalTracks,
        long totalCompleted
) {
}
