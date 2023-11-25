package tech.xavi.soulsync.service.bot;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.entity.*;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.service.auth.AuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
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
            @Value("${tech.xavi.soulsync.gateway.request.spotify.tracks-per-playlist}") int limitVal,
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

    public Playlist createPlaylistEntity(String playlistId, List<SpotifySong> spotifyPlaylist, AddPlaylistReq request){
        Playlist playlist = Playlist.builder()
                .spotifyId(playlistId)
                .cover(getCover(playlistId))
                .name(getName(playlistId))
                .updatable(request.update())
                .lastUpdate(System.currentTimeMillis())
                .avoidDuplicates(request.avoidDuplicates())
                .build();

        if (request.avoidDuplicates()) {
            spotifyPlaylist = new ArrayList<>(spotifyPlaylist.stream()
                    .collect(Collectors.toMap(SpotifySong::getId, song -> song, (existing, replacement) -> existing))
                    .values());
        }

        Set<Song> songList = spotifyPlaylist.stream()
                .map( spotifySong -> convertToEntitySong(spotifySong,playlist))
                .filter(song -> song.getSearchInput() != null && !song.getSearchInput().isEmpty())
                .collect(Collectors.toSet());;

        playlist.setSongs(songList);
        playlist.setLastTotalTracks(songList.size());
        return playlist;
    }

    public void updatePlaylistsFromSpotify() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("[updateAllPlaylists] - Playlist update started");
        playlistRepository
                .findAllUpdatables()
                .forEach(pl -> updatePlaylist(pl.getSpotifyId()));
        stopWatch.stop();
        log.debug("[updateAllPlaylists] - Playlist update finished. Total time elapsed: {} sec",stopWatch.getTotalTimeSeconds());
    }

    private void updatePlaylist(String playlistId){
        AtomicBoolean hasNewSongs = new AtomicBoolean(false);
        playlistRepository
                .findById(playlistId)
                .ifPresent( storedPlaylist -> {
                    updatePlaylistName(storedPlaylist);
                    if (storedPlaylist.isUpdatable()) {
                        int totalTracks = getTotalTracks(playlistId);
                        List<SpotifySong> updatedPl = getPlaylistSongsFromSpotify(playlistId,totalTracks);
                        updatedPl.forEach( spotifySong -> {
                            if (!playlistAlreadyContainsSong(storedPlaylist,spotifySong)) {
                                hasNewSongs.set(true);
                                log.debug("[updatePlaylist] - New song found in playlist {}: {}",
                                        storedPlaylist.getName(),
                                        spotifySong.getName() + " - " + spotifySong.getFirstArtist()
                                );
                                storedPlaylist.getSongs().add(convertToEntitySong(spotifySong,storedPlaylist));
                            }
                        });
                        if (hasNewSongs.get()) {
                            log.debug("[updatePlaylist] - Playlist {} has been updated with new tracks",storedPlaylist.getName());
                            storedPlaylist.setLastTotalTracks(storedPlaylist.getSongs().size());
                            playlistRepository.save(storedPlaylist);
                        }
                    }
                });
    }

    private boolean playlistAlreadyContainsSong(Playlist storedPlaylist, SpotifySong song){
        for (Song storedSong : storedPlaylist.getSongs())
            if (storedSong.getSpotifyId().equals(song.getId())) return true;
        return false;
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
        try {
            return spotifyGateway
                    .getPlaylistTotalTracks(token,playlistId)
                    .getTracks()
                    .getTotal();
        } catch (Exception exception) {
            throw new SoulSyncException(
                    SoulSyncError.PLAYLIST_ID_NOT_VALID,
                    HttpStatus.BAD_REQUEST
            );
        }
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
                .spotifyId(spotifySong.getId())
                .status(SongStatus.WAITING)
                .lastCheck(0)
                .build();
    }

    public PlaylistStatus getPlaylistQueueStatus(String playlistId){
        return queueService.getPlaylistQueueStatus(playlistId);
    }

}
