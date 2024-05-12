package tech.xavi.soulsync.configuration.globals;

public enum PlaylistType {
    PLAYLIST,
    ALBUM,
    SINGLE,
    COMPILATION,
    DISCOGRAPHY;

    public static PlaylistType getType(String type){
        for (PlaylistType pt : values())
            if (pt.name().equalsIgnoreCase(type)) return pt;
        return PLAYLIST;
    }
}
