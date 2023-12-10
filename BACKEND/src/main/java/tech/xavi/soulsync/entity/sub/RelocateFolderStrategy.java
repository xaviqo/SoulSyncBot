package tech.xavi.soulsync.entity.sub;

public enum RelocateFolderStrategy {
    PLAYLIST,
    DISCOGRAPHY;

    public static RelocateFolderStrategy getOption(String value){
        if (PLAYLIST.name().contains(value.toUpperCase()))
            return PLAYLIST;
        return DISCOGRAPHY;
    }
}
