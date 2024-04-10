package tech.xavi.soulsync.dto.playlist;

import lombok.Builder;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.entity.Playlist;

@Builder
public record AddResponseDto(
        Playlist playlist,
        AlertData alertData
) {
}
