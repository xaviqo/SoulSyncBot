package tech.xavi.soulsync.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.dto.gateway.SpotifyPlaylist;
import tech.xavi.soulsync.dto.gateway.SpotifyPlaylistTotalTracks;
import tech.xavi.soulsync.dto.gateway.SpotifyTokenRes;

@Log4j2
@Component
public class SpotifyGateway extends Gateway{

    private final int PL_LIMIT_VALUE;
    private final String AUTH_GET_TOKEN_URL;
    private final String MAIN_GET_PLAYLIST_URL;
    private final String MAIN_GET_TOTAL_TRACKS_URL;
    private final String MAIN_GET_PLAYLIST_FIELDS;
    private final String MAIN_GET_TOTAL_TRACKS_FIELDS;
    private final ObjectMapper objectMapper;


    public SpotifyGateway(
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.auth}") String authBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.main}") String mainBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.auth.get-token.ep}") String authEpGetToken,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-playlist.ep}") String mainEpGetPlaylist,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-total-tracks.ep}") String mainEpGetTotalTracks,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-total-tracks.fields}") String mainFieldsGetTotalTracks,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-playlist.fields}") String mainFieldsGetPlaylist,
            @Value("${tech.xavi.soulsync.cfg.max-req-tracks-per-playlist}") int limitVal,
            ObjectMapper objectMapper
    ) {
        this.AUTH_GET_TOKEN_URL = authBaseUrl+authEpGetToken;
        this.MAIN_GET_PLAYLIST_URL = mainBaseUrl+mainEpGetPlaylist;
        this.MAIN_GET_TOTAL_TRACKS_URL = mainBaseUrl+mainEpGetTotalTracks;
        this.MAIN_GET_PLAYLIST_FIELDS = mainFieldsGetPlaylist;
        this.MAIN_GET_TOTAL_TRACKS_FIELDS = mainFieldsGetTotalTracks;
        this.PL_LIMIT_VALUE = limitVal;
        this.objectMapper = objectMapper;
    }

    public SpotifyTokenRes getToken(String b64credentials) {
        final String grantType = "grant_type";
        final String clientCredentials = "client_credentials";

        try {
            log.debug("[getToken] - URI: {}", AUTH_GET_TOKEN_URL);
            log.debug("[getToken] - Authorization: {}", BASIC_PREFIX + b64credentials);
            log.debug("[getToken] - Payload: {}, {}", grantType, clientCredentials);

            HttpResponse<JsonNode> response = Unirest.post(AUTH_GET_TOKEN_URL)
                    .header(HttpHeaders.AUTHORIZATION, BASIC_PREFIX + b64credentials)
                    .field(grantType, clientCredentials)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getToken] - Response status code: " + responseStatus);

            String responseJson = response.getBody().toString();
            log.debug("[getToken] - Complete response: " + responseJson);

            return objectMapper.readValue(responseJson, SpotifyTokenRes.class);
        } catch (Exception e) {
            log.error("[getToken] - Error while getting token: " + e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylistTotalTracks getPlaylistTotalTracks(String token, String playlistId) {
        try {
            log.debug("[getPlaylistTotalTracks] - URI: {}", MAIN_GET_TOTAL_TRACKS_URL);
            log.debug("[getPlaylistTotalTracks] - Payload: {}, {}", token, playlistId);

            HttpResponse<JsonNode> response = Unirest.get(MAIN_GET_TOTAL_TRACKS_URL)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .routeParam("playlistId", playlistId)
                    .queryString("fields", MAIN_GET_TOTAL_TRACKS_FIELDS)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getPlaylistTotalTracks] - Response status code: " + responseStatus);

            String responseJson = response.getBody().toString();
            log.debug("[getPlaylistTotalTracks] - Complete response: " + responseJson);

            return objectMapper.readValue(responseJson, SpotifyPlaylistTotalTracks.class);
        } catch (Exception e) {
            log.error("[getPlaylistTotalTracks] - Error while getting playlist total tracks: " + e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylist getPlaylistTracks(String token, String playlistId, int offset) {
        try {
            log.debug("[getPlaylistTracks] - URI: {}", MAIN_GET_PLAYLIST_URL);
            log.debug("[getPlaylistTracks] - Payload: {}, {}, {}", token, playlistId,offset);

            HttpResponse<JsonNode> response = Unirest.get(MAIN_GET_PLAYLIST_URL)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .routeParam("playlistId", playlistId)
                    .queryString("fields", MAIN_GET_PLAYLIST_FIELDS)
                    .queryString("limit", PL_LIMIT_VALUE)
                    .queryString("offset", offset)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getPlaylistTracks] - Response status code: " + responseStatus);

            String responseJson = response.getBody().toString();
            log.debug("[getPlaylistTracks] - Complete response: " + responseJson);

            return objectMapper.readValue(responseJson, SpotifyPlaylist.class);
        } catch (Exception e) {
            log.error("[getPlaylistTracks] - Error while getting playlist tracks: " + e.getMessage());
            return null;
        }
    }

}
