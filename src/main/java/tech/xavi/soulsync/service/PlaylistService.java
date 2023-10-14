package tech.xavi.soulsync.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.SpotifySong;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.model.Playlist;
import tech.xavi.soulsync.model.Song;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
public class PlaylistService {

    private final int PL_REQ_LIMIT_VALUE;
    private final SpotifyGateway spotifyGateway;
    private final AuthService authService;
    private final SearchService searchService;
    private final DownloadService downloadService;


    public PlaylistService(
            @Value("${tech.xavi.soulsync.cfg.max-req-tracks-per-playlist}") int limitVal,
            SpotifyGateway spotifyGateway,
            AuthService authService,
            SearchService searchService,
            DownloadService downloadService
    ) {
        this.PL_REQ_LIMIT_VALUE = limitVal;
        this.spotifyGateway = spotifyGateway;
        this.authService = authService;
        this.searchService = searchService;
        this.downloadService = downloadService;
    }

    public void checkPlaylist(Playlist playlist){
        searchService.searchSongs(playlist);
        downloadService.downloadPlaylist(playlist);
    }

    public Playlist convertToPlaylistObject(String playlistId, List<SpotifySong> spotifyPlaylist){
        Playlist playlist = Playlist.builder()
                .spotifyId(playlistId)
                .build();

        List<Song> songList = spotifyPlaylist.stream()
                .map( sptSng -> {
                    String searchInput = searchService
                            .getSongSearchInputForSlskd(sptSng);
                    return Song.builder()
                            .name(sptSng.getName())
                            .artists(sptSng.getArtists())
                            .searchInput(searchInput)
                            .playlist(playlist)
                            .found(false)
                            .build();
                }).toList();

        playlist.setSongs(songList);
        playlist.setLastTotalTracks(songList.size());
        return playlist;
    }

    public int fetchPlaylistTotalTracks(String playlistId){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway
                .getPlaylistTotalTracks(token,playlistId)
                .getTracks()
                .getTotal();
    }

    public List<SpotifySong> getAllPlaylistSongs(String playlistId, int totalTracks) {
        return joinAllFutures(fetchPlaylistDataAsync(playlistId, totalTracks));
    }

    private List<SpotifySong> fetchPlaylist(String playlistId, int offset){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway
                .getPlaylistTracks(token,playlistId,offset)
                .items()
                .stream()
                .toList();
    }

    private List<SpotifySong> joinAllFutures(List<CompletableFuture<List<SpotifySong>>> futures) {
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
    }

    private List<CompletableFuture<List<SpotifySong>>> fetchPlaylistDataAsync(String playlistId, int totalTracks) {
        return IntStream.range(0, (totalTracks + PL_REQ_LIMIT_VALUE - 1) / PL_REQ_LIMIT_VALUE)
                .mapToObj(index -> CompletableFuture.supplyAsync(() -> fetchPlaylist(playlistId, index * PL_REQ_LIMIT_VALUE)))
                .toList();
    }



}
