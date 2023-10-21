package tech.xavi.soulsync.dto.gateway.slskd;

public record SlskdSearchResult(
        SlskdFile[] files,
        boolean hasFreeUploadSlot,
        String username
) {
}
