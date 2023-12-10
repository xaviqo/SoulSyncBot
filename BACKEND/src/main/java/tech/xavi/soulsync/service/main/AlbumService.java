package tech.xavi.soulsync.service.main;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbum;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyArtistAlbums;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.service.auth.AuthService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AuthService authService;
    private final SpotifyGateway spotifyGateway;
    private final SongService songService;

    public SpotifyArtistAlbums getArtistDiscography(String artistId){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway.getArtistAlbums(
                token,
                artistId
        );
    }

    public SpotifyAlbum getAlbum(String albumId){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway.getAlbumTracks(
                token,
                albumId
        );
    }

    public List<SpotifySong> fromSpotifyAlbumToSpotifySongs(SpotifyAlbum spotifyAlbum){
        return spotifyAlbum.getTracks().getItems()
                .stream()
                .map( albumTrack -> songService.mapAlbumTrackToSpotifySong(spotifyAlbum.getName(),albumTrack) )
                .toList();
    }

}
