package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.dto.rest.AddPlaylistReq;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.service.MainService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    private final MainService mainService;

    @PostMapping()
    public ResponseEntity<Playlist> addPlaylist(@RequestBody AddPlaylistReq request){
        return new ResponseEntity<>(
                mainService.addPlaylistRequest(request),
                HttpStatus.ACCEPTED
        );
    }
}
