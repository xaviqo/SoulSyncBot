package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.constants.EndPoint;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.dto.rest.PlaylistDataTable;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.service.rest.AddPlaylistRestService;
import tech.xavi.soulsync.service.rest.GetPlaylistsRestService;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController
public class PlaylistController {

    private final AddPlaylistRestService addPlaylistService;
    private final GetPlaylistsRestService getPlaylistService;

    @PostMapping(EndPoint.PLAYLIST)
    public ResponseEntity<Playlist> addPlaylist(@RequestBody AddPlaylistReq request) throws URISyntaxException {
        return new ResponseEntity<>(
                addPlaylistService.addPlaylistRequest(request),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping(EndPoint.PLAYLIST)
    public ResponseEntity<List<PlaylistDataTable>> getAllPlaylists() {
        return new ResponseEntity<>(
                getPlaylistService.getDataTablePlaylistsInfo(),
                HttpStatus.OK
        );
    }

    @GetMapping(EndPoint.PL_GET_SONGS)
    public ResponseEntity<Set<Song>> getAllPlaylistSongs(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                getPlaylistService.getSongsFromPlaylist(playlistId),
                HttpStatus.OK
        );
    }

    @GetMapping(EndPoint.PL_GET_SONGS_STATS)
    public ResponseEntity<Map<SongStatus,Integer>> getSongsStats(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                getPlaylistService.getAllStatusByPlaylistId(playlistId),
                HttpStatus.OK
        );
    }
}
