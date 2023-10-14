package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;

@Builder
public record SlskdDownloadPayload(String filename, long size) {
}
