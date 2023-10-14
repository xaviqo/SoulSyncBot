package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;

@Builder
public record SlskdSearchQuery(String id, String searchText) {
}
