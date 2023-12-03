package tech.xavi.soulsync.service.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.rest.PlaylistDataTable;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.bot.PlaylistService;

import java.util.*;

@RequiredArgsConstructor
@Service
public class GetPlaylistsRestService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final PlaylistService playlistService;

    public Set<Song> getSongsFromPlaylist(String playlistId){
        return playlistService.getPlaylistById(playlistId).getSongs();
    }

    public List<PlaylistDataTable> getDataTablePlaylistsInfo(){
        List<PlaylistDataTable> playlists = new ArrayList<>();
        playlistRepository
                .fetchPlaylistsDatatable()
                .forEach( pl -> {
                    int totalCompletedOrCopied =
                            getTotalSongsByStatus(pl.getSpotifyId(),SongStatus.COMPLETED)
                                    + getTotalSongsByStatus(pl.getSpotifyId(),SongStatus.COPIED);
                    playlists.add(
                            PlaylistDataTable
                                    .builder()
                                    .name(pl.getName())
                                    .id(pl.getSpotifyId())
                                    .cover(pl.getCover())
                                    .added(pl.getAdded())
                                    .type(pl.getType())
                                    .lastUpdate(pl.getLastUpdate())
                                    .total(pl.getLastTotalTracks())
                                    .totalSucceeded(totalCompletedOrCopied)
                                    .build()
                    );
                });
        return playlists;
    }

    public Map<SongStatus,Integer> getAllStatusByPlaylistId(String playlistId){
        Map<SongStatus,Integer> playlistStatus = new HashMap<>();
        for (SongStatus status : SongStatus.values()) {
            int statusTotal = getTotalSongsByStatus(playlistId,status);
            playlistStatus.put(status, statusTotal);
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
