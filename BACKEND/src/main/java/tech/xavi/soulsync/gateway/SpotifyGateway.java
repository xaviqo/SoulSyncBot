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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static tech.xavi.soulsync.service.main.PlaylistService.PL_REQ_LIMIT_VALUE;

@Log4j2
@Component
public class SpotifyGateway extends Gateway{

    private final String AUTH_GET_TOKEN_URL;
    private final String PL_GET_TRACKS;
    private final String PL_GET_DATA;
    private final String PL_GET_COVER;
    private final String ARTIST_GET_ALBUMS;
    private final String ALBUM_GET_TRACKS;
    private long retryThreshold;
    private final Map<String,String> responsesMap;


    public SpotifyGateway(
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.auth}") String authBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.main}") String mainBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.auth.get-token.ep}") String authEpGetToken,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.playlist.get-tracks.ep}") String mainEpGetPlaylist,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.playlist.get-playlist-data.ep}") String mainEpPlaylistData,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.artist.get-albums.ep}") String artistEpGetAlbums,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.album.get-tracks.ep}") String albumEpGetTracks,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.playlist.get-cover.ep}") String mainEpGetCover,
            ObjectMapper objectMapper
    ) {
        super(objectMapper);
        this.AUTH_GET_TOKEN_URL = authBaseUrl+authEpGetToken;
        this.PL_GET_TRACKS = mainBaseUrl+mainEpGetPlaylist;
        this.PL_GET_DATA = mainBaseUrl+mainEpPlaylistData;
        this.PL_GET_COVER = mainBaseUrl+mainEpGetCover;
        this.ARTIST_GET_ALBUMS = mainBaseUrl+artistEpGetAlbums;
        this.ALBUM_GET_TRACKS = mainBaseUrl+albumEpGetTracks;
        this.responsesMap = new HashMap<>();
    }

    @Override
    public <U> U callMapped(ExternalApi api, GatewayRequest request, Class<U> responseClass) {
        if (isRateLimitThresholdPassed()) {
            HttpResponse<String> response = call(api,request);
            if (response.getStatus() != 429) {
                responsesMap.put(
                        request.getUrl(),
                        response.getBody()
                );
            } else {
                int retryAfter = Integer.parseInt(response.getHeaders().get("retry-after").get(0));
                setRateLimitThreshold(retryAfter);
            }
        }
        return mapResponseBody(
                api,
                request,
                responsesMap.get(request.getUrl()),
                responseClass
        );
    }

    public SpotifyPlaylistName getPlaylistName(String token, String playlistId){
        return callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(PL_GET_DATA)
                        .token(token)
                        .routeParams(Map.of("playlistId",playlistId))
                        .queryStrings(Map.of("fields","name"))
                        .build(),
                SpotifyPlaylistName.class
        );
    }

    public SpotifyPlaylistCover[] getPlaylistCover(String token, String playlistId){
        return super.callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(PL_GET_COVER)
                        .token(token)
                        .routeParams(Map.of("playlistId",playlistId))
                        .build(),
                SpotifyPlaylistCover[].class
        );
    }

    public SpotifyPlaylistTotalTracks getPlaylistTotalTracks(String token, String playlistId) {
        return callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(PL_GET_DATA)
                        .token(token)
                        .routeParams(Map.of("playlistId",playlistId))
                        .queryStrings(Map.of("fields","tracks.total"))
                        .build(),
                SpotifyPlaylistTotalTracks.class
        );
    }

    public SpotifyPlaylist getPlaylistTracks(String token, String playlistId, int offset) {
        final Map<String,Object> queryStrings = new HashMap<>() {{
            put("fields", "items(track(name,album(name),artists(name),id))");
            put("limit", PL_REQ_LIMIT_VALUE);
            put("offset", offset);
        }};
        return callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(PL_GET_TRACKS)
                        .token(token)
                        .routeParams(Map.of("playlistId",playlistId))
                        .queryStrings(queryStrings)
                        .build(),
                SpotifyPlaylist.class
        );
    }

    public SpotifyAlbum getAlbumTracks(String token, String albumId){
        return super.callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(ALBUM_GET_TRACKS)
                        .token(token)
                        .routeParams(Map.of("albumId",albumId))
                        .queryStrings(Map.of("limit","50"))
                        .build(),
                SpotifyAlbum.class
        );
    }

    public SpotifyArtistAlbums getArtistAlbums(String token, String artistId){
        final Map<String,Object> queryStrings = new HashMap<>() {{
            put("include_groups", "album");
            put("limit", 50);
        }};
        return super.callMapped(
                ExternalApi.SPOTIFY,
                GatewayRequest.builder()
                        .method(HttpMethod.GET)
                        .url(ARTIST_GET_ALBUMS)
                        .token(token)
                        .routeParams(Map.of("artistId",artistId))
                        .queryStrings(queryStrings)
                        .build(),
                SpotifyArtistAlbums.class
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

    private void setRateLimitThreshold(int retrySec) {
        this.retryThreshold = Instant.now().getEpochSecond()+retrySec;
    }

    private boolean isRateLimitThresholdPassed() {
        return this.retryThreshold < Instant.now().getEpochSecond();
    }
}
