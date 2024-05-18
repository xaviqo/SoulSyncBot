package tech.xavi.soulsync.dto.playlist;

import lombok.Builder;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.shared.AlertData;

@Builder
public record AddResponseDto(
        String id,
        PlaylistType playlistType,
        String name,
        String cover,
        int totalTracks,
        String owner,
        AlertData alertData
) {
}
