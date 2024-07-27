package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.playlist.AddPlaylistDto;
import tech.xavi.soulsync.dto.playlist.AddResponseDto;
import tech.xavi.soulsync.dto.playlist.PlaylistOverviewDto;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.service.playlist.AddPlaylistService;
import tech.xavi.soulsync.service.playlist.GetPlaylistService;
import tech.xavi.soulsync.service.playlist.RemovePlaylistService;
import tech.xavi.soulsync.service.playlist.UpdatePlaylistService;
import tech.xavi.soulsync.service.song.SongService;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class PlaylistController {

    private final GetPlaylistService getPlaylistService;
    private final AddPlaylistService addPlaylistService;
    private final UpdatePlaylistService updatePlaylistService;
    private final RemovePlaylistService removePlaylistService;
    private final SongService songService;

    @GetMapping(ApiRoutes.EP_PLAYLIST_SONGS)
    public ResponseEntity<Page<SpotifySong>> getPlaylistSongs(@PathVariable String id, Pageable pageable){
        return ResponseEntity.ok(songService.findByPlaylistsId(id,pageable));
    }

    @GetMapping(ApiRoutes.EP_PLAYLIST_DISCOGRAPHY)
    public ResponseEntity<Set<PlaylistOverviewDto>> getPlaylistDiscography(@PathVariable String id){
        return ResponseEntity.ok(getPlaylistService.getPlaylistDiscography(id));
    }

    @GetMapping(ApiRoutes.EP_PLAYLIST + "/{id}")
    public ResponseEntity<PlaylistOverviewDto> getPlaylist(@PathVariable String id){
        return ResponseEntity.ok(getPlaylistService.getPlaylist(id));
    }

    @GetMapping(ApiRoutes.EP_PLAYLIST)
    public ResponseEntity<Set<PlaylistOverviewDto>> getAllPlaylists(){
        return ResponseEntity.ok(getPlaylistService.getAllPlaylists());
    }

    @GetMapping(ApiRoutes.EP_PLAYLIST_FORCE_UPDATE)
    public ResponseEntity<Void> forceUpdate(@PathVariable String playlistId){
        updatePlaylistService.forceUpdatePlaylist(playlistId);
        return ResponseEntity.ok(null);
    }

    @PostMapping(ApiRoutes.EP_PLAYLIST)
    public ResponseEntity<AddResponseDto> addPlaylist(@RequestBody AddPlaylistDto addPlaylistRequest){
        return ResponseEntity.ok(addPlaylistService.handleAddPlaylistRequest(addPlaylistRequest));
    }

    @DeleteMapping(ApiRoutes.EP_PLAYLIST + "/{id}")
    public ResponseEntity<Void> removePlaylist(@PathVariable String id) {
        removePlaylistService.remove(id);
        return ResponseEntity.ok(null);
    }

}
