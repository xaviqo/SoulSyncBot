package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.dto.rest.ResponseWrapper;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.service.request.AddPlaylistRequestService;
import tech.xavi.soulsync.service.request.GetPlaylistsRequestService;

import java.net.URISyntaxException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/playlist")
public class PlaylistController {

    private final AddPlaylistRequestService addPlaylistService;
    private final GetPlaylistsRequestService getPlaylistService;

    @PostMapping()
    public ResponseEntity<ResponseWrapper<Playlist>> addPlaylist(@RequestBody AddPlaylistReq request) throws URISyntaxException {
        return new ResponseEntity<>(
                ResponseWrapper.wrapResponse(
                        addPlaylistService.addPlaylistRequest(request),
                        Playlist.class
                ),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<?>> getAllPlaylists() {
        return new ResponseEntity<>(
                ResponseWrapper.wrapResponse(
                        getPlaylistService.getDataTablePlaylistsInfo(),
                        List.class
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("songs")
    public ResponseEntity<ResponseWrapper<?>> getAllPlaylistSongs(@RequestParam String playlistId) {
        return new ResponseEntity<>(
                ResponseWrapper.wrapResponse(
                        getPlaylistService.getSongsFromPlaylist(playlistId),
                        List.class
                ),
                HttpStatus.OK
        );
    }
}
