package tech.xavi.soulsync.service.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.PlaylistStatus;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.repository.PlaylistRepository;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Log4j2
@Service
public class PlaylistService {

    private final int PL_REQ_LIMIT_VALUE;
    private final SpotifyGateway spotifyGateway;
    private final AuthService authService;
    private final SearchService searchService;
    private final QueueService queueService;
    private final PlaylistRepository playlistRepository;


    public PlaylistService(
            @Value("${tech.xavi.soulsync.cfg.max-req-tracks-per-playlist}") int limitVal,
            SpotifyGateway spotifyGateway,
            AuthService authService,
            SearchService searchService,
            QueueService queueService,
            PlaylistRepository playlistRepository
    ) {
        this.PL_REQ_LIMIT_VALUE = limitVal;
        this.spotifyGateway = spotifyGateway;
        this.authService = authService;
        this.searchService = searchService;
        this.queueService = queueService;
        this.playlistRepository = playlistRepository;
    }

    public Playlist createPlaylistEntity(String playlistId, List<SpotifySong> spotifyPlaylist){
        Playlist playlist = Playlist.builder()
                .spotifyId(playlistId)
                .cover(getCover(playlistId))
                .name(getName(playlistId))
                .updatable(true)
                .lastUpdate(System.currentTimeMillis())
                .build();

        List<Song> songList = spotifyPlaylist.stream()
                .map( spotifySong -> convertToEntitySong(spotifySong,playlist))
                .filter(song -> song.getSearchInput() != null && !song.getSearchInput().isEmpty())
                .toList();

        playlist.setSongs(songList);
        playlist.setLastTotalTracks(songList.size());
        return playlist;
    }

    public void updateAllPlaylists() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("[updateAllPlaylists] - Playlist update started");
        playlistRepository
                .findAllSpotifyId()
                .forEach(this::updatePlaylist);
        stopWatch.stop();
        log.debug("[updateAllPlaylists] - Playlist update finished. Total time elapsed: {} sec",stopWatch.getTotalTimeSeconds());
    }

    private void updatePlaylist(String playlistId){
        playlistRepository
                .findById(playlistId)
                .ifPresent( storedPlaylist -> {
                    updatePlaylistName(storedPlaylist);
                    if (storedPlaylist.isUpdatable()) {
                        int totalTracks = getTotalTracks(playlistId);
                        List<Song> storedSongs = storedPlaylist.getSongs();
                        int beforeTotal = storedSongs.size();
                        getPlaylistSongsFromSpotify(playlistId,totalTracks)
                                .forEach( spotifySong -> {
                                    if (!playlistContainsSpotifySong(storedSongs,spotifySong)) {
                                        log.debug("[updatePlaylistTracks] - New song found in playlist {}: {}",
                                                storedPlaylist.getName(),
                                                spotifySong.getName() + " - " + spotifySong.getFirstArtist()
                                        );
                                        storedSongs.add(convertToEntitySong(spotifySong,storedPlaylist));
                                    }
                                });
                        int afterTotal = storedSongs.size();
                        if (afterTotal > beforeTotal) {
                            log.debug("[updatePlaylistTracks] - Playlist {} has been updated with new tracks. " +
                                            "Previous Total: {} - New total: {}",
                                    storedPlaylist.getName(),
                                    beforeTotal,
                                    afterTotal
                            );
                            storedPlaylist.setSongs(storedSongs);
                            playlistRepository.save(storedPlaylist);
                        }
                    }
                });

    }

    private boolean playlistContainsSpotifySong(List<Song> storedSongs, SpotifySong spotifySong){
        for (Song stored : storedSongs) {
            boolean isNameEqual = stored.getName().equalsIgnoreCase(spotifySong.getName());
            boolean areArtistsEqual =  isArtistsArraysEqual(stored,spotifySong);
            if (isNameEqual && areArtistsEqual) return true;
        }
        return false;
    }

    private boolean isArtistsArraysEqual(Song storedSong, SpotifySong spotifySong){
        return storedSong.getArtists().size() == spotifySong.getArtists().size()
                && new HashSet<>(storedSong.getArtists()).containsAll(spotifySong.getArtists());
    }

    private void updatePlaylistName(Playlist playlist){
        String spotifyName = getName(playlist.getSpotifyId());
        if (!spotifyName.equalsIgnoreCase(playlist.getName())) {
            log.debug("[updatePlaylistName] - Playlist with name update. Previous: '{}' - New: '{}'",
                    playlist.getName(),
                    spotifyName
            );
            playlist.setName(spotifyName);
        }
    }

    public int getTotalTracks(String playlistId){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway
                .getPlaylistTotalTracks(token,playlistId)
                .getTracks()
                .getTotal();
    }

    public String getName(String playlistId){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway
                .getPlaylistName(token,playlistId)
                .name();
    }

    public String getCover(String playlistId){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway
                .getPlaylistCover(token,playlistId)[0]
                .url();
    }

    public List<SpotifySong> getPlaylistSongsFromSpotify(String playlistId, int totalTracks) {
        return joinAllFutures(fetchPlaylistDataAsync(playlistId, totalTracks));
    }

    private List<SpotifySong> joinAllFutures(List<CompletableFuture<List<SpotifySong>>> futures) {
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
    }

    private List<CompletableFuture<List<SpotifySong>>> fetchPlaylistDataAsync(String playlistId, int totalTracks) {
        return IntStream.range(0, (totalTracks + PL_REQ_LIMIT_VALUE - 1) / PL_REQ_LIMIT_VALUE)
                .mapToObj(index -> CompletableFuture.supplyAsync(() -> fetchPlaylistFromSpotify(playlistId, index * PL_REQ_LIMIT_VALUE)))
                .toList();
    }

    private List<SpotifySong> fetchPlaylistFromSpotify(String playlistId, int offset){
        String token = authService.getSpotifyToken().token();
        return spotifyGateway
                .getPlaylistTracks(token,playlistId,offset)
                .items()
                .stream()
                .toList();
    }

    private Song convertToEntitySong(SpotifySong spotifySong, Playlist playlist) {
        String searchInput = searchService
                .getSongSearchInputForSlskd(spotifySong);
        return Song.builder()
                .searchId(UUID.randomUUID())
                .name(spotifySong.getName())
                .artists(spotifySong.getArtists())
                .searchInput(searchInput)
                .playlist(playlist)
                .found(false)
                .build();
    }

    public PlaylistStatus getPlaylistStatus(String id, int remaining){
        return (remaining == 0)
                ? PlaylistStatus.COMPLETED
                : queueService.getPlaylistQueueStatus(id);
    }


}
