package tech.xavi.soulsync.dto.gateway.slskd;

import lombok.Builder;

@Builder
public record SlskdSearchStatus(
        int fileCount,
        String id,
        boolean isComplete
) {
}
