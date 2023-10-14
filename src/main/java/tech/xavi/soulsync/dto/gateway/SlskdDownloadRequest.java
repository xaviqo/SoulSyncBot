package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;

@Builder
public record SlskdDownloadRequest(SlskdDownloadPayload payload, String username) {
}
