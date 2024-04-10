package tech.xavi.soulsync.configuration.globals;

public enum PlaylistType {
    PLAYLIST,
    ALBUM,
    SINGLE,
    COMPILATION;

    public static PlaylistType getPlaylistType(String type){
        for (PlaylistType pt : values())
            if (pt.name().equalsIgnoreCase(type)) return pt;
        return PLAYLIST;
    }
}
