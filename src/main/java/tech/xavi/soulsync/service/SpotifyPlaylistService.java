package tech.xavi.soulsync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.dto.gateway.SpotifySong;

import java.text.Normalizer;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class SpotifyPlaylistService {

    private final SpotifyGateway spotifyGateway;

    private final AuthService authService;
    private final String TEST_PL = "62i69EEjXQ9fajydsBQKbT";


    public int getPlaylistTotalTracks(String playlistId){
        String token = authService.getToken();
        return spotifyGateway
                .getPlaylistTotalTracks(token,playlistId)
                .getTracks()
                .getTotal();
    }

    public Stream<SpotifySong> getPlaylist(String playlistId){
        String token = authService.getToken();
        return spotifyGateway
                .getPlaylistTracks(token,playlistId)
                .items()
                .stream();
    }

    private String removeSpecialChars(String songNameAndArtist){
        final String SPECIAL_CHARS_REGEX = "[^a-zA-Z0-9]";
        final String DIACRITICAL_ACCENT_MARKS_REGEX = "\\p{M}";
        final String TWO_OR_MORE_SPACES_REGEX = "\\s+";
        if (songNameAndArtist == null || songNameAndArtist.isBlank())
            return "";
        else
            return Normalizer.normalize(songNameAndArtist, Normalizer.Form.NFKD)
                    .replaceAll("ñ","n")
                    .replaceAll("&"," and ")
                    .replaceAll("ç","c")
                    .replaceAll(DIACRITICAL_ACCENT_MARKS_REGEX, "")
                    .replaceAll(SPECIAL_CHARS_REGEX," ")
                    .replaceAll(TWO_OR_MORE_SPACES_REGEX, " ")
                    .trim();
    }


}
