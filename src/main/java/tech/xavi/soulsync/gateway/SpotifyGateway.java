package tech.xavi.soulsync.gateway;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tech.xavi.soulsync.dto.gateway.SpotifyPlaylistResponse;
import tech.xavi.soulsync.dto.gateway.SpotifyPlaylistTotalTracksResponse;
import tech.xavi.soulsync.dto.gateway.SpotifyTokenResponse;

@Component
public class SpotifyGateway extends Gateway{

    private final String FIELDS_PARAM = "fields";
    private final String AUTH_BASE_URL;
    private final String MAIN_BASE_URL;
    private final String AUTH_GET_TOKEN_PATH;
    private final String MAIN_GET_PLAYLIST_PATH;
    private final String MAIN_GET_TOTAL_TRACKS_PATH;
    private final String MAIN_GET_PLAYLIST_FIELDS;
    private final String MAIN_GET_TOTAL_TRACKS_FIELDS;


    public SpotifyGateway(
            @Value("${tech.xavi.soulsync.gateway.scheme.spotify}") String scheme,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.auth}") String authBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.base-url.spotify.main}") String mainBaseUrl,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.auth.get-token.ep}") String authEpGetToken,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-playlist.ep}") String mainEpGetPlaylist,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-total-tracks.ep}") String mainEpGetTotalTracks,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-total-tracks.fields}") String mainFieldsGetTotalTracks,
            @Value("${tech.xavi.soulsync.gateway.path.spotify.main.get-playlist.fields}") String mainFieldsGetPlaylist,
            WebClient webClient
    ) {
        this.SCHEME = scheme;
        this.AUTH_BASE_URL = authBaseUrl;
        this.MAIN_BASE_URL = mainBaseUrl;
        this.AUTH_GET_TOKEN_PATH = authEpGetToken;
        this.MAIN_GET_PLAYLIST_PATH = mainEpGetPlaylist;
        this.MAIN_GET_TOTAL_TRACKS_PATH = mainEpGetTotalTracks;
        this.MAIN_GET_PLAYLIST_FIELDS = mainFieldsGetPlaylist;
        this.MAIN_GET_TOTAL_TRACKS_FIELDS = mainFieldsGetTotalTracks;
        this.webClient = webClient;
    }

    public SpotifyTokenResponse getToken(String b64credentials){
        final String grantType = "grant_type";
        final String clientCredentials = "client_credentials";
        return webClient.post()
                .uri( uriBuilder -> uriBuilder
                        .scheme(SCHEME)
                        .host(AUTH_BASE_URL)
                        .path(AUTH_GET_TOKEN_PATH)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION,BASIC_PREFIX+b64credentials)
                .body(BodyInserters.fromFormData(grantType, clientCredentials))
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class)
                .block();
    }

    public SpotifyPlaylistTotalTracksResponse getPlaylistTotalTracks(String token, String playlistId){
        return webClient.get()
                .uri( uriBuilder -> uriBuilder
                        .scheme(SCHEME)
                        .host(MAIN_BASE_URL)
                        .path(MAIN_GET_TOTAL_TRACKS_PATH)
                        .queryParam(FIELDS_PARAM,MAIN_GET_TOTAL_TRACKS_FIELDS)
                        .build(playlistId)
                )
                .header(HttpHeaders.AUTHORIZATION,BEARER_PREFIX+token)
                .retrieve()
                .bodyToMono(SpotifyPlaylistTotalTracksResponse.class)
                .block();
    }

    public SpotifyPlaylistResponse getPlaylistTracks(String token, String playlistId){
        return webClient.get()
                .uri( uriBuilder -> uriBuilder
                        .scheme(SCHEME)
                        .host(MAIN_BASE_URL)
                        .path(MAIN_GET_PLAYLIST_PATH)
                        .queryParam(FIELDS_PARAM,MAIN_GET_PLAYLIST_FIELDS)
                        .build(playlistId)
                )
                .header(HttpHeaders.AUTHORIZATION,BEARER_PREFIX+token)
                .retrieve()
                .bodyToMono(SpotifyPlaylistResponse.class)
                .block();
    }

}
