package tech.xavi.soulsync.service.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.rest.PlaylistDataTable;
import tech.xavi.soulsync.entity.PlaylistStatus;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.SongStatus;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.bot.PlaylistService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class GetPlaylistsRestService {

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
                    int totalCompletedOrCopied =
                            getTotalSongsByStatus(pl.getSpotifyId(),SongStatus.COMPLETED)
                                    + getTotalSongsByStatus(pl.getSpotifyId(),SongStatus.COPIED);
                    PlaylistStatus status = (totalCompletedOrCopied >= pl.getLastTotalTracks())
                            ? PlaylistStatus.COMPLETED
                            : playlistService.getPlaylistQueueStatus(pl.getSpotifyId());
                    playlists.add(
                            PlaylistDataTable
                                    .builder()
                                    .name(pl.getName())
                                    .id(pl.getSpotifyId())
                                    .cover(pl.getCover())
                                    .lastUpdate(pl.getLastUpdate())
                                    .total(pl.getLastTotalTracks())
                                    .status(status)
                                    .totalSucceeded(totalCompletedOrCopied)
                                    .build()
                    );
                });
        return playlists;
    }

    public Map<SongStatus,Integer> getAllStatusByPlaylistId(String playlistId){
        Map<SongStatus,Integer> playlistStatus = new HashMap<>();
        for (SongStatus status : SongStatus.values()) {
            playlistStatus.put(
                    status,
                    getTotalSongsByStatus(playlistId,status)
            );
        }
        return playlistStatus;
    }

    private int getTotalSongsByStatus(String playlistId, SongStatus status){
        return songRepository
                .countSongsByStatusByPlaylistId(
                        playlistId,
                        status
                );
    }

}
