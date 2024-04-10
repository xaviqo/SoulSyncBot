package tech.xavi.soulsync.repository.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.xavi.soulsync.configuration.globals.RequestType;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.GatewayToken;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.dto.gateway.spotify.TrackContainerDto;

import java.util.HashMap;
import java.util.Map;

@Component
public class SpotifyPlaylistGateway extends Gateway {

    private final String GET_PLAYLIST_PATH;
    private final String GET_ALBUM_PATH;
    private static final String PLAYLIST_FIELDS = "name,images,tracks(total)";
    private static final String SONG_FIELDS = "items(track(id,name,album(id,name),artists(id,name)))";
    private static final int PL_REQ_LIMIT_VALUE = 20;

    public SpotifyPlaylistGateway(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.main}") String mainBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.playlist.get-playlist.ep}") String pathGetPlaylist,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.album.get-album.ep}") String pathGetAlbum
            ) {
        super(restTemplate, objectMapper);
        setBaseUrl(mainBaseUrl);
        this.GET_PLAYLIST_PATH = pathGetPlaylist;
        this.GET_ALBUM_PATH = pathGetAlbum;
    }

    public SpotifyPlaylistDto getPlaylistDetails(GatewayToken token, String playlistId, RequestType reqType){
        final String path = reqType.equals(RequestType.ALBUM)
                ? GET_ALBUM_PATH
                : GET_PLAYLIST_PATH;
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(path)
                        .routeParams(Map.of("playlistId",playlistId))
                        .query(Map.of("fields",PLAYLIST_FIELDS))
                        .build(),
                SpotifyPlaylistDto.class
        );
    }

    public TrackContainerDto getPlaylistSongs(GatewayToken token, String playlistId, RequestType reqType, int offset){
        final String path = (reqType.equals(RequestType.ALBUM)
                ? GET_ALBUM_PATH
                : GET_PLAYLIST_PATH) + "/tracks";
        final Map<String,Object> queryStrings = new HashMap<>() {{
            put("fields", SONG_FIELDS);
            put("limit", PL_REQ_LIMIT_VALUE);
            put("offset", offset);
        }};
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(path)
                        .routeParams(Map.of("playlistId",playlistId))
                        .query(queryStrings)
                        .build(),
                TrackContainerDto.class
        );
    }


}
