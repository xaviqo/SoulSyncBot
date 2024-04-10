package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.playlist.AddPlaylistDto;
import tech.xavi.soulsync.dto.playlist.AddResponseDto;
import tech.xavi.soulsync.service.playlist.AddPlaylistService;

@RestController @RequiredArgsConstructor
public class PlaylistController {

    private final AddPlaylistService addPlaylistService;

    @PostMapping(ApiRoutes.EP_PLAYLIST)
    public ResponseEntity<AddResponseDto> addPlaylist(@RequestBody AddPlaylistDto addPlaylistRequest){
        return ResponseEntity.ok(addPlaylistService.handleAddPlaylistRequest(addPlaylistRequest));
    }

}
