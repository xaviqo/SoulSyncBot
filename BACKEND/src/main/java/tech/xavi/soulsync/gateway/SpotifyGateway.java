package tech.xavi.soulsync.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.dto.gateway.spotify.*;

@Log4j2
@Component
public class SpotifyGateway extends Gateway{

    private final int PL_LIMIT_VALUE;
    private final String AUTH_GET_TOKEN_URL;
    private final String MAIN_GET_TRACKS;
    private final String MAIN_GET_DATA;
    private final String MAIN_GET_COVER;
    private final ObjectMapper objectMapper;


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
        this.AUTH_GET_TOKEN_URL = authBaseUrl+authEpGetToken;
        this.MAIN_GET_TRACKS = mainBaseUrl+mainEpGetPlaylist;
        this.MAIN_GET_DATA = mainBaseUrl+mainEpPlaylistData;
        this.MAIN_GET_COVER = mainBaseUrl+mainEpGetCover;
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

    public SpotifyPlaylistName getPlaylistName(String token, String playlistId){
        final String fields = "name";
        try {
            log.debug("[getPlaylistName] - URI: {}", MAIN_GET_DATA);
            log.debug("[getPlaylistName] - Payload: {}, {}", token, playlistId);

            HttpResponse<JsonNode> response = Unirest.get(MAIN_GET_DATA)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .routeParam("playlistId", playlistId)
                    .queryString("fields", fields)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getPlaylistName] - Response status code: " + responseStatus);

            String responseJson = response.getBody().toString();
            log.debug("[getPlaylistName] - Complete response: " + responseJson);

            return objectMapper.readValue(responseJson, SpotifyPlaylistName.class);
        } catch (Exception e) {
            log.error("[getPlaylistName] - Error while getting playlist name: " + e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylistCover[] getPlaylistCover(String token, String playlistId){
        try {
            log.debug("[getPlaylistCover] - URI: {}", MAIN_GET_COVER);
            log.debug("[getPlaylistCover] - Payload: {}, {}", token, playlistId);

            HttpResponse<JsonNode> response = Unirest.get(MAIN_GET_COVER)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .routeParam("playlistId", playlistId)
                    .asJson();

            int responseStatus = response.getStatus();
            log.debug("[getPlaylistCover] - Response status code: " + responseStatus);

            String responseJson = response.getBody().toString();
            log.debug("[getPlaylistCover] - Complete response: " + responseJson);

            return objectMapper.readValue(responseJson, SpotifyPlaylistCover[].class);
        } catch (Exception e) {
            log.error("[getPlaylistCover] - Error while getting playlist name: " + e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylistTotalTracks getPlaylistTotalTracks(String token, String playlistId) {
        final String fields = "tracks.total";
        try {
            log.debug("[getPlaylistTotalTracks] - URI: {}", MAIN_GET_DATA);
            log.debug("[getPlaylistTotalTracks] - Payload: {}, {}", token, playlistId);

            HttpResponse<JsonNode> response = Unirest.get(MAIN_GET_DATA)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .routeParam("playlistId", playlistId)
                    .queryString("fields", fields)
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
        final String fields = "items(track(name,artists(name),id))";
        try {
            log.debug("[getPlaylistTracks] - URI: {}", MAIN_GET_TRACKS);
            log.debug("[getPlaylistTracks] - Payload: {}, {}, {}", token, playlistId,offset);

            HttpResponse<JsonNode> response = Unirest.get(MAIN_GET_TRACKS)
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                    .routeParam("playlistId", playlistId)
                    .queryString("fields",fields)
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
