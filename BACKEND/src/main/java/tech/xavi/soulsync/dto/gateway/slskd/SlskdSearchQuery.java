package tech.xavi.soulsync.dto.gateway.slskd;

import lombok.Builder;

@Builder
public record SlskdSearchQuery(String id, String searchText) {
}
