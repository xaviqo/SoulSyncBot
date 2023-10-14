package tech.xavi.soulsync.dto.gateway;

public record SlskdSearchResult(
        SlskdFile[] files,
        boolean hasFreeUploadSlot,
        String username
) {
}
