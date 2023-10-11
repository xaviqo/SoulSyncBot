package tech.xavi.soulsync.model;

import java.util.List;

public record Playlist(
        String spotifyId,
        List<Song>songs
) {

}
