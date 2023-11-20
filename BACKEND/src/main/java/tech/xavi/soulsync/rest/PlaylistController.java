package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.dto.rest.PlaylistDataTable;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.SongStatus;
import tech.xavi.soulsync.service.rest.AddPlaylistRestService;
import tech.xavi.soulsync.service.rest.GetPlaylistsRestService;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/playlist")
public class PlaylistController {

    private final AddPlaylistRestService addPlaylistService;
    private final GetPlaylistsRestService getPlaylistService;

    @PostMapping()
    public ResponseEntity<Playlist> addPlaylist(@RequestBody AddPlaylistReq request) throws URISyntaxException {
        return new ResponseEntity<>(
                addPlaylistService.addPlaylistRequest(request),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping
    public ResponseEntity<List<PlaylistDataTable>> getAllPlaylists() {
        return new ResponseEntity<>(
                getPlaylistService.getDataTablePlaylistsInfo(),
                HttpStatus.OK
        );
    }

    @GetMapping("songs")
    public ResponseEntity<List<Song>> getAllPlaylistSongs(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                getPlaylistService.getSongsFromPlaylist(playlistId),
                HttpStatus.OK
        );
    }

    @GetMapping("stats")
    public ResponseEntity<Map<SongStatus,Integer>> getSongsStats(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                getPlaylistService.getAllStatusByPlaylistId(playlistId),
                HttpStatus.OK
        );
    }
}
