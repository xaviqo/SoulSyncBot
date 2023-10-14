package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;

import java.util.List;

@Builder
public record SpotifyPlaylist(List<SpotifySong> items) {}
