package tech.xavi.soulsync.dto.playlist;

import lombok.Builder;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.db.Artist;

import java.util.List;

@Builder
public record SlskdRequestDto(
        long id,
        String name,
        List<Artist> artists,
        String album,
        String searchInput,
        ProcessStatus status,
        long attempts,
        long lastCheck,
        long added,
        String copyRoute,
        String filename,
        long size,
        int bitRate,
        String sharedBy
) {

}
