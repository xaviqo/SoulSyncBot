package tech.xavi.soulsync.dto.gateway.slskd;

import lombok.Builder;

import java.util.List;

@Builder
public record SlskdSearchResult(
        int fileCount,
        String id,
        boolean isComplete,
        List<SlskdSearchResponse> responses
) {
}