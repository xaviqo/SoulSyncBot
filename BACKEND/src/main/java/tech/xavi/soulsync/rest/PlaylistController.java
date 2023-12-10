package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.constants.EndPoint;
import tech.xavi.soulsync.dto.service.AddPlaylistReq;
import tech.xavi.soulsync.dto.service.PausePlaylistReq;
import tech.xavi.soulsync.dto.service.PlaylistDataTable;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.PlaylistType;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.service.rest.PlaylistRestService;
import tech.xavi.soulsync.service.rest.QueueRestService;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController
public class PlaylistController {

    private final PlaylistRestService playlistRestService;
    private final QueueRestService queueRestService;

    @PostMapping(EndPoint.PLAYLIST)
    public ResponseEntity<Playlist> addPlaylist(@RequestBody AddPlaylistReq request) throws URISyntaxException {
        playlistRestService.addPlaylistRequest(request);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping(EndPoint.PLAYLIST)
    public ResponseEntity<List<PlaylistDataTable>> getAllPlaylists() {
        return new ResponseEntity<>(
                playlistRestService.getDataTablePlaylistsInfo(),
                HttpStatus.OK
        );
    }

    @DeleteMapping(EndPoint.PLAYLIST)
    public ResponseEntity<?> removePlaylist(@RequestParam String playlistId){
        playlistRestService.removePlaylist(playlistId);
        queueRestService.pausePlaylist(playlistId,false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(EndPoint.PL_TYPES)
    public ResponseEntity<PlaylistType[]> getPlaylistTypes() {
        return new ResponseEntity<>(
                PlaylistType.values(),
                HttpStatus.OK
        );
    }

    @GetMapping(EndPoint.PL_GET_SONGS)
    public ResponseEntity<Set<Song>> getAllPlaylistSongs(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                playlistRestService.getSongsFromPlaylist(playlistId),
                HttpStatus.OK
        );
    }

    @GetMapping(EndPoint.PL_GET_SONGS_STATS)
    public ResponseEntity<Map<SongStatus,Integer>> getSongsStats(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                playlistRestService.getAllStatusByPlaylistId(playlistId),
                HttpStatus.OK
        );
    }

    @GetMapping(EndPoint.PL_PAUSE_SEARCH)
    public ResponseEntity<?> isPlaylistPaused(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                queueRestService.isPlaylistPaused(playlistId),
                HttpStatus.OK
        );
    }

    @PostMapping(EndPoint.PL_PAUSE_SEARCH)
    public ResponseEntity<?> pausePlaylistSearch(@RequestBody PausePlaylistReq pauseReq) {
        queueRestService.pausePlaylist(pauseReq.playlistId(),pauseReq.pause());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
