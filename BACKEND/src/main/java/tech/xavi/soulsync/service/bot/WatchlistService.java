package tech.xavi.soulsync.service.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;
@RequiredArgsConstructor
@Log4j2
@Service
public class WatchlistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public void updateWatchlist(Playlist playlist){
        log.debug("[addToWatchlist] - Playlist is saved in DB: {}",playlist.getSpotifyId());
        playlistRepository.save(playlist);
    }

    public void updateSongStatus(Song song){
        log.debug("[updateSongStatus] - Song is saved in DB: {}",song.getSearchInput());
        songRepository.save(song);
    }

}
