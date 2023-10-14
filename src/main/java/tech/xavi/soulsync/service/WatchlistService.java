package tech.xavi.soulsync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.model.Playlist;
import tech.xavi.soulsync.repository.PlaylistRepository;

import java.util.List;
@RequiredArgsConstructor
@Log4j2
@Service
public class WatchlistService {

    private final PlaylistRepository playlistRepository;

    public List<Playlist> getWaitingPlaylists(){
        return playlistRepository
                .fetchUnfoundSongsInPlaylists();
    }

    public void updateWatchlist(Playlist playlist){
        log.debug("[addToWatchlist] - Playlist is saved in DB: {}",playlist.getSpotifyId());
        playlistRepository.save(playlist);
    }

}
