package tech.xavi.soulsync.entity.sub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpotifyPathType {
    PLAYLIST("/playlist/"),
    ARTIST("/artist/"),
    ALBUM("/album/");
    private final String path;

    public static SpotifyPathType findPathTypeByURL(String requestUrl){
        for (SpotifyPathType pathType : values()){
            if (requestUrl.contains(pathType.getPath())) return pathType;
        }
        return null;
    }
}
