package tech.xavi.soulsync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.SpotifySong;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.model.Playlist;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        int totalTracks = playlistService
                .fetchPlaylistTotalTracks(playlistId);

        if (totalTracks < 1) {
            return null;
        }

        List<SpotifySong> spotifyPlaylist = playlistService
                .getAllPlaylistSongs(playlistId, totalTracks);
        Playlist playlist = playlistService
                .convertToPlaylistObject(playlistId,spotifyPlaylist);
        watchlistService.updateWatchlist(playlist);

        CompletableFuture
                .runAsync(() -> {
                    queueService.addPlaylistToQueue(playlist);
                    queueService.removePlaylistFromQueue(playlist);
                });

        return playlist;
    }

    public void slskdHealthCheckOnInit(){
        if (slskdGateway.healthCheck())
            log.info("[healthCheck] - Slsk API connection ----> SUCCESS");
        else
            log.error("[healthCheck] - Slsk API connection ----> FAIL");
    }





}
