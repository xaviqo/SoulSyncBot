package tech.xavi.soulsync.dto.gateway.slskd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SlskdFile(
        int bitRate,
        long size,
        String filename,
        String id,
        String state,
        String username
) {
}