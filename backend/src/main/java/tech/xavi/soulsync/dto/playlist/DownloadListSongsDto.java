package tech.xavi.soulsync.dto.playlist;

import lombok.Builder;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;

import java.util.Set;

@Builder
public record DownloadListSongsDto(
        DownloadList downloadList,
        Set<SlskdRequest> listSongs
) {
}
