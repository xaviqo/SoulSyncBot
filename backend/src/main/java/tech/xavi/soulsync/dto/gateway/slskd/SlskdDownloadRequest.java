package tech.xavi.soulsync.dto.gateway.slskd;

import lombok.Builder;

@Builder
public record SlskdDownloadRequest(SlskdFile payload, String username) {
}
