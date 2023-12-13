package tech.xavi.soulsync.service.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.security.SoulSyncException;
import tech.xavi.soulsync.dto.service.AddPlaylistReq;
import tech.xavi.soulsync.dto.service.PlaylistDataTable;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.entity.sub.SoulSyncError;
import tech.xavi.soulsync.entity.sub.SpotifyPathType;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.main.PlaylistService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class PlaylistRestService {

    private final SongRepository songRepository;
    private final PlaylistService playlistService;
    private final PlaylistTypeSaverService playlistTypeSaverService;

    public void addPlaylistRequest(AddPlaylistReq request) throws URISyntaxException {

        if (!isValidURL(request.playlist())) {
            throw new SoulSyncException(
                    SoulSyncError.NOT_A_URL,
                    HttpStatus.BAD_REQUEST
            );
        }
        SpotifyPathType pathType = SpotifyPathType.findPathTypeByURL(request.playlist());
        String id = obtainIdFromRequest(request.playlist(),pathType);
        playlistTypeSaverService.handleUserRequest(id,pathType);

    }

    private String obtainIdFromRequest(String requestInput, SpotifyPathType pathType) throws URISyntaxException {
        String typePath = pathType.getPath();
        URI uri = new URI(requestInput);
        String path = uri.getPath();

        int typeIndex = path.indexOf(typePath);
        if (typeIndex != -1) {
            int startIndex = typeIndex + typePath.length();
            int endIndex = path.indexOf("/", startIndex);

            return (endIndex == -1)
                    ? path.substring(startIndex)
                    : path.substring(startIndex, endIndex);
        }
        throw new SoulSyncException(
                SoulSyncError.URL_NOT_VALID,
                HttpStatus.BAD_REQUEST
        );
    }

    private boolean isValidURL(String string){
        return string
                .substring(0,4)
                .equalsIgnoreCase("http");
    }

    public void removePlaylist(String playlistId){
        Playlist playlist = playlistService.getPlaylistById(playlistId);
        List<Song> songsToRemove = playlist.getSongs().stream()
                .filter(song -> song.getPlaylists().size() == 1)
                .toList();
        playlistService.removePlaylist(playlist);
        songRepository.deleteAll(songsToRemove);
    }

    public Set<Song> getSongsFromPlaylist(String playlistId){
        return playlistService.getPlaylistById(playlistId).getSongs();
    }

    public List<PlaylistDataTable> getDataTablePlaylistsInfo(){
        List<PlaylistDataTable> playlists = new ArrayList<>();
        playlistService
                .fetchPlaylistsDatatable()
                .forEach( pl -> {
                    int totalCompletedOrCopied =
                            getTotalSongsByStatus(pl.getSpotifyId(), SongStatus.COMPLETED)
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
