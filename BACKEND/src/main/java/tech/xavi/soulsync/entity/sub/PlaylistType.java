package tech.xavi.soulsync.entity.sub;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlaylistType {
    PLAYLIST("Playlist"),
    ALBUM("Album"),
    DISCOGRAPHY("Discography")
    ;
    private final String name;

}
