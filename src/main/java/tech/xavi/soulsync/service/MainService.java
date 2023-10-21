package tech.xavi.soulsync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.entity.Playlist;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class MainService {

    private final SlskdGateway slskdGateway;
    private final PlaylistService playlistService;
    private final QueueService queueService;
    private final WatchlistService watchlistService;

    // TODO: Refactor totalTracks, find another solution
    public Playlist addPlaylistRequest(AddPlaylistReq request){
        String playlistId = request.playlist();

        int totalTracks = playlistService.getTotalTracks(playlistId);


        List<SpotifySong> spotifyPlaylist = playlistService
                .getPlaylistSongsFromSpotify(playlistId, totalTracks);
        Playlist playlist = playlistService
                .createPlaylistEntity(playlistId,spotifyPlaylist);
        watchlistService
                .updateWatchlist(playlist);

        queueService.seek(playlist);

        return playlist;
    }

    public void slskdHealthCheckOnInit(){
        if (slskdGateway.isSlskdApiHealthy())
            log.info("[healthCheck] - Slsk API connection ----> SUCCESS");
        else
            log.error("[healthCheck] - Slsk API connection ----> FAIL");
    }





}
