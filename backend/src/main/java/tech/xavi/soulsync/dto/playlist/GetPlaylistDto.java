package tech.xavi.soulsync.dto.playlist;

import lombok.Builder;
import tech.xavi.soulsync.configuration.globals.PlaylistType;

@Builder
public record GetPlaylistDto(
        String id,
        PlaylistType playlistType,
        String cover,
        String name,
        String owner,
        int totalTracks
) {
}
