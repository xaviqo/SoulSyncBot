package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyPlaylistDto;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.service.artist.ArtistMainService;
import tech.xavi.soulsync.service.integration.SpotifyGatewayService;
import tech.xavi.soulsync.service.song.SongService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service @RequiredArgsConstructor
public class UpdatePlaylistService {

    private final PlaylistService playlistService;
    private final ArtistMainService artistMainService;
    private final SongService songService;
    private final SpotifyGatewayService spotifyGatewayService;

    public void updatePlaylist(Playlist currentPlaylist){
        SpotifyPlaylistDto updatedPlaylist = spotifyGatewayService.getPlaylistDetails(currentPlaylist.getId());
        if (updateName(updatedPlaylist,currentPlaylist) || updateTracklist(updatedPlaylist,currentPlaylist)) {
            currentPlaylist.setLastUpdate(System.currentTimeMillis());
            playlistService.savePlaylist(currentPlaylist);
        }
    }

    private boolean updateName(SpotifyPlaylistDto updated, Playlist current){
        boolean isNameUpdated = !updated.getName().equalsIgnoreCase(current.getName());
        if (isNameUpdated) current.setName(updated.getName());
        return isNameUpdated;
    }

    private boolean updateTracklist(SpotifyPlaylistDto updated, Playlist current){
        boolean isTracklistUpdated = updated.getTotalTracks() != current.getTotalTracks();
        if (isTracklistUpdated) {
            Hibernate.initialize(current.getSongs());
            List<String> currentSongsIds = current
                    .getSongs()
                    .stream()
                    .map(SpotifySong::getSpotifyId)
                    .toList();
            Set<SpotifySong> newSongs = songService
                    .fetchSongsFromSpotify(updated)
                    .stream()
                    .parallel()
                    .filter( spotifySong -> isNewSong(currentSongsIds,spotifySong) )
                    .peek(spotifySong -> spotifySong.setArtists(new ArrayList<>(spotifySong.getArtists())) )
                    .collect(Collectors.toSet());

            artistMainService.saveArtistsFromTracklist(newSongs);
            songService.saveSongs(newSongs);

            Set<SpotifySong> combinedSongs = Stream
                    .concat(current.getSongs().stream(), newSongs.stream())
                    .collect(Collectors.toSet());
            current.setSongs(combinedSongs);
        }
        return isTracklistUpdated;
    }

    private boolean isNewSong(List<String> songsIds, SpotifySong songToCheck){
        return songsIds
                .stream()
                .parallel()
                .noneMatch(id -> id.equals(songToCheck.getSpotifyId()));
    }
}
