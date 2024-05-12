package tech.xavi.soulsync.service.task.maintenance;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.service.artist.ArtistMainService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;
import tech.xavi.soulsync.service.playlist.PlaylistCreationService;
import tech.xavi.soulsync.service.playlist.PlaylistMainService;
import tech.xavi.soulsync.service.song.SongService;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class UpdatePlaylistProcess extends MaintenanceProcess {

    private final PlaylistMainService playlistMainService;
    private final ArtistMainService artistMainService;
    private final SongService songService;
    private final PlaylistCreationService playlistCreationService;
    private final SpotifyGatewayService spotifyGatewayService;
    @Getter private final int order = 10;

    @Override
    public CompletableFuture<Void> execute() {
        playlistMainService
                .findAllByType(PlaylistType.PLAYLIST)
                .forEach(this::updatePlaylist);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void updatePlaylist(Playlist currentPlaylist){
        SpotifyPlaylistDto updatedPlaylist = spotifyGatewayService.getPlaylistDetails(currentPlaylist.getId());
        if (updateName(updatedPlaylist,currentPlaylist) || updateTracklist(updatedPlaylist,currentPlaylist)) {
            currentPlaylist.setLastUpdate(System.currentTimeMillis());
            playlistMainService.savePlaylist(currentPlaylist);
        }
    }

    private boolean updateName(SpotifyPlaylistDto updated, Playlist current){
        boolean isNameUpdated = !updated.getName().equalsIgnoreCase(current.getName());
        if (isNameUpdated)
            current.setName(updated.getName());
        return isNameUpdated;
    }

    protected boolean updateTracklist(SpotifyPlaylistDto updated, Playlist current){
        boolean isTracklistUpdated = updated.getTotalTracks() != current.getTotalTracks();
        if (isTracklistUpdated) {
            Hibernate.initialize(current.getSongs());
            Set<SpotifySong> newSongs = playlistCreationService
                    .getTracklist(updated)
                    .stream()
                    .parallel()
                    .filter( spotifySong -> isNewSong(current,spotifySong) )
                    .collect(Collectors.toSet());
            artistMainService.saveArtistsFromTracklist(newSongs);
            songService.saveTracklist(newSongs);
            current.getSongs().addAll(newSongs);
        }
        return isTracklistUpdated;
    }

    private boolean isNewSong(Playlist playlist, SpotifySong songToCheck){
        return playlist.getSongs()
                .stream()
                .parallel()
                .noneMatch(spotifySong ->
                        spotifySong
                                .getSpotifyId()
                                .equals(songToCheck.getSpotifyId())
                );
    }

    @Override
    public String getTaskName() {
        return "UPDATE_PLAYLIST";
    }

}
