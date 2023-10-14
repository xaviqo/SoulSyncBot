package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;

@Builder
public record SlskdSearchStatus(
        int fileCount,
        String id,
        boolean isComplete
) {
}
