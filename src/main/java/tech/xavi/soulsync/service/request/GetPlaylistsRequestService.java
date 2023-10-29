package tech.xavi.soulsync.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.rest.PlaylistDataTable;
import tech.xavi.soulsync.entity.PlaylistStatus;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.process.PlaylistService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GetPlaylistsRequestService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final PlaylistService playlistService;

    public List<Song> getSongsFromPlaylist(String playlistId){
        return songRepository
                .getSongsByPlaylistId(playlistId);
    }

    public List<PlaylistDataTable> getDataTablePlaylistsInfo(){
        List<PlaylistDataTable> playlists = new ArrayList<>();
        playlistRepository
                .fetchPlaylistsDatatable()
                .forEach( pl -> {
                    int totalNotFound = songRepository
                        .getTotalUnfoundSongsFromPlaylist(pl.getSpotifyId());
                    PlaylistStatus status = playlistService
                            .getPlaylistStatus(
                                    pl.getSpotifyId(),
                                    totalNotFound
                            );
                    playlists.add(
                            PlaylistDataTable
                                    .builder()
                                    .name(pl.getName())
                                    .id(pl.getSpotifyId())
                                    .cover(pl.getCover())
                                    .remaining(totalNotFound)
                                    .lastUpdate(pl.getLastUpdate())
                                    .total(pl.getLastTotalTracks())
                                    .status(status)
                                    .build()
                    );
                });
        return playlists;
    }
}
