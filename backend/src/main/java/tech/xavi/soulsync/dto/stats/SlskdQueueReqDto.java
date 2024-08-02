package tech.xavi.soulsync.dto.stats;

import lombok.Builder;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.db.Artist;

import java.util.List;

@Builder
public record SlskdQueueReqDto(
        String name,
        List<Artist> artists,
        ProcessStatus status,
        String playlistCover,
        String searchInput,
        long lastCheck,
        long attempts
) {
}
