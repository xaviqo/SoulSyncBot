package tech.xavi.soulsync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.model.Playlist;
import tech.xavi.soulsync.model.Song;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;

import java.util.List;
@RequiredArgsConstructor
@Log4j2
@Service
public class WatchlistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;


    public List<Playlist> getWaitingPlaylists(){
        return playlistRepository
                .fetchUnfoundSongsInPlaylists();
    }

    public void updateWatchlist(Playlist playlist){
        log.debug("[addToWatchlist] - Playlist is saved in DB: {}",playlist.getSpotifyId());
        playlistRepository.save(playlist);
    }

    public void updateSongStatus(Song song){
        log.debug("[updateSongStatus] - Song is saved in DB: {}",song.getSearchInput());
        songRepository.save(song);
    }

}
