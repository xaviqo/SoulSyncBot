package tech.xavi.soulsync.service.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyImageDto;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.repository.db.PlaylistRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service @RequiredArgsConstructor
public class PlaylistMainService {

    private final PlaylistRepository playlistRepository;

    public Set<Playlist> findAll(){
        return playlistRepository.findAll();
    }

    public Stream<Playlist> findAllByType(PlaylistType type){
        return playlistRepository
                .findAllByPlaylistType(type);
    }

    public Optional<Playlist> findById(String id){
        return playlistRepository.findById(id);
    }

    public boolean existsById(String id) {
        return playlistRepository
                .existsById(id);
    }

    public Playlist savePlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public String getPlaylistCoverUrl(SpotifyImageDto[] spotifyImageArr){
        if (spotifyImageArr.length > 0)
            return spotifyImageArr[0].getUrl();
        return null;
    }

}
