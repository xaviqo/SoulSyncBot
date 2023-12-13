package tech.xavi.soulsync.entity.sub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import tech.xavi.soulsync.configuration.security.SoulSyncException;

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
        throw new SoulSyncException(
                SoulSyncError.URL_NOT_VALID,
                HttpStatus.BAD_REQUEST
        );
    }
}
