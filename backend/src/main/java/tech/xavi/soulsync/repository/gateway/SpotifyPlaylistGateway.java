package tech.xavi.soulsync.repository.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.GatewayToken;
import tech.xavi.soulsync.dto.gateway.spotify.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class SpotifyPlaylistGateway extends Gateway {

    private static final String TRACKS_ROUTE = "/tracks";
    private final String GET_PLAYLIST_PATH;
    private final String GET_ARTIST_DISCOGRAPHY_PATH;
    private final String GET_ALBUM_PATH;
    private static final String PLAYLIST_FIELDS = "name,images,tracks(total)";
    private static final String SONG_FIELDS = "items(track(id,name,album(id,name),artists(id,name)))";
    public static final int MAX_SONGS_PER_REQUEST = 50;
    public static final int MAX_ALBUMS_PER_REQUEST = 20;

    public SpotifyPlaylistGateway(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.main}") String mainBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.playlist.get-playlist.ep}") String pathGetPlaylist,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.album.get-album.ep}") String pathGetAlbum,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.artist.get-discography.ep}") String pathGetArtistsDisc
            ) {
        super(restTemplate, objectMapper,false);
        setBaseUrl(mainBaseUrl);
        this.GET_PLAYLIST_PATH = pathGetPlaylist;
        this.GET_ALBUM_PATH = pathGetAlbum;
        this.GET_ARTIST_DISCOGRAPHY_PATH = pathGetArtistsDisc;
    }

    public SpotifyDiscographyDto getArtistDiscography(GatewayToken token, String artistId) {
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(GET_ARTIST_DISCOGRAPHY_PATH)
                        .routeParams(Map.of("artistId",artistId))
                        .build(),
                SpotifyDiscographyDto.class
        );
    }

    public SpotifyAlbumContainerDto getAlbumsTracks(GatewayToken token, String albumIdsByComa) {
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(GET_ALBUM_PATH)
                        .query(Map.of("ids",albumIdsByComa))
                        .build(),
                SpotifyAlbumContainerDto.class
        );
    }

    public SpotifyPlaylistDto getPlaylistDetails(GatewayToken token, String playlistId){
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(GET_PLAYLIST_PATH)
                        .routeParams(Map.of("playlistId",playlistId))
                        .query(Map.of("fields",PLAYLIST_FIELDS))
                        .build(),
                SpotifyPlaylistDto.class
        );
    }

    public SpotifyPlaylistTrackContainerDto getPlaylistSongs(GatewayToken token, String playlistId, int offset){
        final Map<String,Object> queryStrings = new HashMap<>() {{
            put("fields", SONG_FIELDS);
            put("limit", MAX_SONGS_PER_REQUEST);
            put("offset", offset);
        }};
        return mappedCall(
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .token(token.token())
                        .path(GET_PLAYLIST_PATH + TRACKS_ROUTE)
                        .routeParams(Map.of("playlistId",playlistId))
                        .query(queryStrings)
                        .build(),
                SpotifyPlaylistTrackContainerDto.class
        );
    }


}
