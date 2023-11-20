package tech.xavi.soulsync.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.dto.gateway.GatewayRequest;
import tech.xavi.soulsync.dto.gateway.spotify.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class SpotifyGateway extends Gateway{

    private final int PL_LIMIT_VALUE;
    private final String AUTH_GET_TOKEN_URL;
    private final String MAIN_GET_TRACKS;
    private final String MAIN_GET_DATA;
    private final String MAIN_GET_COVER;


    public SpotifyGateway(
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.auth}") String authBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.main}") String mainBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.auth.get-token.ep}") String authEpGetToken,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-tracks.ep}") String mainEpGetPlaylist,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-playlist-data.ep}") String mainEpPlaylistData,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-cover.ep}") String mainEpGetCover,
            @Value("${tech.xavi.soulsync.gateway.request.spotify.tracks-per-playlist}") int limitVal,
            ObjectMapper objectMapper
    ) {
        super(objectMapper);
        this.AUTH_GET_TOKEN_URL = authBaseUrl+authEpGetToken;
        this.MAIN_GET_TRACKS = mainBaseUrl+mainEpGetPlaylist;
        this.MAIN_GET_DATA = mainBaseUrl+mainEpPlaylistData;
        this.MAIN_GET_COVER = mainBaseUrl+mainEpGetCover;
        this.PL_LIMIT_VALUE = limitVal;
    }

    public SpotifyPlaylistName getPlaylistName(String token, String playlistId){
        return callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(MAIN_GET_DATA)
                        .token(token)
                        .routeParams(Map.entry("playlistId",playlistId))
                        .queryStrings(Map.of("fields","name"))
                        .build(),
                SpotifyPlaylistName.class
        );
    }

    public SpotifyPlaylistCover[] getPlaylistCover(String token, String playlistId){
        return callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(MAIN_GET_COVER)
                        .token(token)
                        .routeParams(Map.entry("playlistId",playlistId))
                        .build(),
                SpotifyPlaylistCover[].class
        );
    }

    public SpotifyPlaylistTotalTracks getPlaylistTotalTracks(String token, String playlistId) {
        return callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(MAIN_GET_DATA)
                        .token(token)
                        .routeParams(Map.entry("playlistId",playlistId))
                        .queryStrings(Map.of("fields","tracks.total"))
                        .build(),
                SpotifyPlaylistTotalTracks.class
        );
    }

    public SpotifyPlaylist getPlaylistTracks(String token, String playlistId, int offset) {
        final Map<String,Object> queryStrings = new HashMap<>() {{
            put("fields", "items(track(name,artists(name),id))");
            put("limit", PL_LIMIT_VALUE);
            put("offset", offset);
        }};
        return callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(MAIN_GET_TRACKS)
                        .token(token)
                        .routeParams(Map.entry("playlistId",playlistId))
                        .queryStrings(queryStrings)
                        .build(),
                SpotifyPlaylist.class
        );
    }

    public SpotifyTokenRes getToken(String b64credentials) {
        return callMapped(
                ExternalApi.SPOTIFY.method("getToken"),
                GatewayRequest.builder()
                        .method(HttpMethod.POST)
                        .url(AUTH_GET_TOKEN_URL)
                        .basicCredentials(b64credentials)
                        .appJson(false)
                .build(),
        SpotifyTokenRes.class
        );
    }

    public boolean isSpotifyApiHealthy(){
        try {
            log.debug("[healthCheck] - SPOTIFY - URI: {}",AUTH_GET_TOKEN_URL);
            HttpResponse<String> response = Unirest.get(AUTH_GET_TOKEN_URL).asString();
            log.debug("[healthCheck] - SPOTIFY - Response status code: {}", response.getStatus());
            return response.getStatus() < 500;
        } catch (Exception e) {
            log.error("[healthCheck] - SPOTIFY - Unable to connect to the SPOTIFY API");
            return false;
        }
    }

}
