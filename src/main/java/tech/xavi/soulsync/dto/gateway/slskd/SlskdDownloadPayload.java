package tech.xavi.soulsync.dto.gateway.slskd;

import lombok.Builder;

@Builder
public record SlskdDownloadPayload(String filename, long size) {
}
