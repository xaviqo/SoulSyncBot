package tech.xavi.soulsync.configuration.globals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestType {
    PLAYLIST("/playlist/",true,"Playlist '%s' added, with a total of %s tracks"),
    ARTIST("/artist/",false,"Discography of artist %s added"),
    ALBUM("/album/",false,"Album %s by artist %s added");
    private final String path;
    private final boolean isUpdatable;
    private final String userMessage;

}
