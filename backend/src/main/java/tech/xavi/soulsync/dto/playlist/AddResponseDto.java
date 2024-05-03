package tech.xavi.soulsync.dto.playlist;

import lombok.Builder;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.shared.AlertData;

@Builder
public record AddResponseDto(
        PlaylistType playlistType,
        String playlistName,
        String playlistCover,
        int totalTracks,
        AlertData alertData
) {
}
