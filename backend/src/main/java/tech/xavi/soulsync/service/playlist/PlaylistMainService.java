package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.repository.db.PlaylistRepository;

import java.util.Set;

@Service @RequiredArgsConstructor
public class PlaylistMainService {

    private final PlaylistRepository playlistRepository;

    public Set<Playlist> findAllByType(PlaylistType type){
        return playlistRepository
                .findAllByPlaylistType(PlaylistType.PLAYLIST);
    }

    public boolean existPlaylistBySpotifyId(String spotifyId) {
        return playlistRepository
                .existsBySpotifyId(spotifyId);
    }

    public Playlist savePlaylist(Playlist playlist) {
        return playlistRepository
                .save(playlist);
    }

}
