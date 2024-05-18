package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.configuration.SearchPolicyDto;
import tech.xavi.soulsync.dto.playlist.AddPlaylistDto;
import tech.xavi.soulsync.dto.playlist.AddResponseDto;
import tech.xavi.soulsync.dto.playlist.PlaylistOverviewDto;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListService;
import tech.xavi.soulsync.service.playlist.AddPlaylistService;
import tech.xavi.soulsync.service.playlist.GetPlaylistService;
import tech.xavi.soulsync.service.search.SearchPolicyService;
import tech.xavi.soulsync.service.song.SongService;

import java.util.Set;

@RestController @RequiredArgsConstructor
public class PlaylistController {

    private final GetPlaylistService getPlaylistService;
    private final AddPlaylistService addPlaylistService;
    private final SongService songService;
    private final DownloadListService downloadListService;
    private final SearchPolicyService searchPolicyService;

    @GetMapping(ApiRoutes.EP_PLAYLIST_SONGS)
    public ResponseEntity<Page<SpotifySong>> getPlaylistSongs(@PathVariable String id, Pageable pageable){
        return ResponseEntity.ok(songService.findByPlaylistsId(id,pageable));
    }

    @GetMapping(ApiRoutes.EP_PLAYLIST_DOWNLOADS)
    public ResponseEntity<Set<DownloadList>> getPlaylistDownloads(@PathVariable String id){
        return ResponseEntity.ok(downloadListService.getPlaylistDownloadLists(id));
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

    @PostMapping(ApiRoutes.EP_PLAYLIST)
    public ResponseEntity<AddResponseDto> addPlaylist(@RequestBody AddPlaylistDto addPlaylistRequest){
        return ResponseEntity.ok(addPlaylistService.handleAddPlaylistRequest(addPlaylistRequest));
    }

    @GetMapping(ApiRoutes.EP_SEARCH_POLICY)
    public ResponseEntity<?> getSearchPolicyConfiguration(){
        return ResponseEntity.ok(searchPolicyService.getAllPolicies());
    }

    @GetMapping(ApiRoutes.EP_SEARCH_POLICY + "/{id}")
    public ResponseEntity<SearchPolicy> getSearchPolicyById(@PathVariable String id){
        return ResponseEntity.ok(searchPolicyService.getPolicyById(id));
    }

    @PostMapping(ApiRoutes.EP_SEARCH_POLICY)
    public ResponseEntity<SearchPolicyDto> upsertSearchPolicy(@RequestBody SearchPolicyDto searchPolicyReq){
        return ResponseEntity.ok(searchPolicyService.upsert(searchPolicyReq));
    }

    @DeleteMapping(ApiRoutes.EP_SEARCH_POLICY + "/{id}")
    public ResponseEntity<Void> deleteSearchPolicy(@PathVariable String id){
        searchPolicyService.deletePolicyById(id);
        return ResponseEntity.noContent().build();
    }

}
