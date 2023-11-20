package tech.xavi.soulsync.dto.gateway.slskd;

import lombok.Builder;

@Builder
public record SlskdDownloadRequest(SlskdDownloadPayload payload, String username) {
}
