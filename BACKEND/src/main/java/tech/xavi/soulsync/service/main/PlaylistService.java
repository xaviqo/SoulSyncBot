package tech.xavi.soulsync.service.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyAlbum;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifySong;
import tech.xavi.soulsync.dto.projection.PlaylistProjection;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.PlaylistType;
import tech.xavi.soulsync.entity.sub.SoulSyncError;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.service.auth.AuthService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Log4j2
@Service
public class PlaylistService {

    private static final int PL_UPDATE_THRESHOLD_MS = 1800000;
    public static final int PL_REQ_LIMIT_VALUE = 20;
    private final SpotifyGateway spotifyGateway;
    private final AuthService authService;
    private final PlaylistRepository playlistRepository;
    private final SongService songService;

    public Playlist savePlaylist(String playlistId) {
        Playlist playlist = Playlist.builder()
                .spotifyId(playlistId)
                .cover(getCover(playlistId))
                .name(getName(playlistId))
                .updatable(true)
                .lastUpdate(System.currentTimeMillis())
                .added(System.currentTimeMillis())
                .type(PlaylistType.PLAYLIST)
                .build();
        playlistRepository.save(playlist);
        return playlist;
    }

    public Playlist saveAlbum(SpotifyAlbum album){
        Playlist playlist = Playlist.builder()
                .spotifyId(album.getId())
                .cover(album.getCover())
                .name(album.getName())
                .updatable(false)
                .lastUpdate(0)
                .added(System.currentTimeMillis())
                .type(PlaylistType.ALBUM)
                .lastTotalTracks(album.getTotalTracks())
                .build();
        playlistRepository.save(playlist);
        return playlist;
    }

    public void addSongsToPlaylist(Playlist playlist, List<SpotifySong> spotifySongList){
        spotifySongList.stream()
                .collect(Collectors
                        .toMap(
                                SpotifySong::getId,
                                songService::getOrCreateSong,
                                (existing, replace) -> existing)
                )
                .values()
                .stream()
                .filter(song -> song.getSearchInput() != null && !song.getSearchInput().isEmpty())
                .forEach(playlist::addSong);
        playlist.setLastTotalTracks(playlist.getSongs().size());
        playlistRepository.save(playlist);
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
                    if (storedPlaylist.isUpdatable() && passesUpdateThreshold(storedPlaylist)) {
                        List<SpotifySong> updatedPl = getPlaylistSongsFromSpotify(playlistId);
                        updatedPl.forEach( spotifySong -> {
                            if (!playlistAlreadyContainsSong(storedPlaylist,spotifySong)) {
                                hasNewSongs.set(true);
                                log.debug("[updatePlaylist] - New song found in playlist {}: {}",
                                        storedPlaylist.getName(),
                                        spotifySong.getName() + " - " + spotifySong.getFirstArtist()
                                );
                                storedPlaylist.getSongs().add(songService.getOrCreateSong(spotifySong));
                            }
                        });
                        if (hasNewSongs.get()) {
                            log.debug("[updatePlaylist] - Playlist {} has been updated with new tracks",storedPlaylist.getName());
                            storedPlaylist.setLastTotalTracks(storedPlaylist.getSongs().size());
                        }
                        storedPlaylist.setLastUpdate(System.currentTimeMillis());
                        playlistRepository.save(storedPlaylist);
                    }
                });
    }

    private boolean passesUpdateThreshold(Playlist playlist){
        long plThreshold = playlist.getLastUpdate()+PL_UPDATE_THRESHOLD_MS;
        return plThreshold <= System.currentTimeMillis();
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

    public List<SpotifySong> getPlaylistSongsFromSpotify(String playlistId) {
        return joinAllFutures(fetchPlaylistDataAsync(playlistId));
    }

    private List<SpotifySong> joinAllFutures(List<CompletableFuture<List<SpotifySong>>> futures) {
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
    }

    private List<CompletableFuture<List<SpotifySong>>> fetchPlaylistDataAsync(String playlistId) {
        int totalTracks = getTotalTracks(playlistId);
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

    public List<PlaylistProjection> fetchPlaylistsDatatable(){
        return playlistRepository.fetchPlaylistsDatatable();
    }

    public boolean isPlaylistInDB(String playlistId){
        return playlistRepository.playlistExists(playlistId) > 0;
    }

    public Playlist getPlaylistById(String playlistId){
        return playlistRepository.findBySpotifyId(playlistId)
                .orElse(new Playlist());
    }

    public void removePlaylist(Playlist playlist){
        playlistRepository.delete(playlist);
    }

}
