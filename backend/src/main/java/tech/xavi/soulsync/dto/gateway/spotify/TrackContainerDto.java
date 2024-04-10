package tech.xavi.soulsync.dto.gateway.spotify;

public record TrackContainerDto(
        SpotifySongDto[] items
) {
}
