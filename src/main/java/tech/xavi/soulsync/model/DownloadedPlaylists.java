package tech.xavi.soulsync.model;

import org.springframework.stereotype.Component;

import java.util.List;

public record DownloadedPlaylists(List<Playlist> playlists) {
}
