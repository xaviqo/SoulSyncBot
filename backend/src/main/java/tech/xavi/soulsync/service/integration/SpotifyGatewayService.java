package tech.xavi.soulsync.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.configuration.globals.RequestType;
import tech.xavi.soulsync.dto.gateway.GatewayToken;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySongDto;
import tech.xavi.soulsync.repository.gateway.SpotifyPlaylistGateway;

import java.util.Arrays;
import java.util.List;

@Service @RequiredArgsConstructor
public class SpotifyGatewayService {

    private final GatewayTokenService gatewayTokenService;
    private final SpotifyPlaylistGateway spotifyPlaylistGateway;

    public SpotifyPlaylistDto getPlaylistDetails(
            String playlistId,
            RequestType reqType
    ){
        SpotifyPlaylistDto dto = spotifyPlaylistGateway.getPlaylistDetails(
                getToken(),
                playlistId,
                reqType
        );
        dto.setId(playlistId);
        return dto;
    }


    public List<SpotifySongDto> getPlaylistSongs(
            String playlistId,
            RequestType reqType,
            int offset
    ){
        return Arrays.asList(spotifyPlaylistGateway.getPlaylistSongs(
                getToken(),
                playlistId,
                reqType,
                offset
        ).items());
    }

    private GatewayToken getToken(){
        return gatewayTokenService
                .getToken(GatewayName.SPOTIFY);
    }



}
