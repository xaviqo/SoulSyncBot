package tech.xavi.soulsync.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.dto.gateway.GatewayToken;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbumDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySongDto;
import tech.xavi.soulsync.repository.gateway.SpotifyPlaylistGateway;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Service @RequiredArgsConstructor
public class SpotifyGatewayService {

    private final GatewayTokenService gatewayTokenService;
    private final SpotifyPlaylistGateway spotifyPlaylistGateway;

    public SpotifyPlaylistDto getPlaylistDetails(String playlistId){
        SpotifyPlaylistDto dto = spotifyPlaylistGateway
                .getPlaylistDetails(getToken(), playlistId);
        dto.setId(playlistId);
        return dto;
    }

    public SpotifyAlbumDto[] getArtistDiscography(String artistId){
        return spotifyPlaylistGateway.getArtistDiscography(
                getToken(),
                artistId
        ).items();
    }

    public SpotifyAlbumDto[] getAlbumWithTracks(String albumIdsByComa) {
        return spotifyPlaylistGateway.getAlbumsTracks(
                getToken(),
                albumIdsByComa
        );
    }

    public List<SpotifySongDto> getPlaylistSongs(
            String playlistId,
            int offset
    ){
        return Arrays.asList(spotifyPlaylistGateway.getPlaylistSongs(
                getToken(),
                playlistId,
                offset
        ).items());
    }

    private GatewayToken getToken(){
        GatewayToken tkn = gatewayTokenService
                .getToken(GatewayName.SPOTIFY);
        log.debug("Spotify token: {}", tkn);
        return tkn;
    }



}
