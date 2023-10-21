package tech.xavi.soulsync.dto.gateway.slskd;

public record SlskdFile(
        int bitRate,
        long size,
        String filename
) {
}
