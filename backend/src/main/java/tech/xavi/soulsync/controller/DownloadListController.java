package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.downloadlist.DownloadListDto;
import tech.xavi.soulsync.dto.downloadlist.DownloadListProcessDto;
import tech.xavi.soulsync.dto.playlist.SlskdRequestDto;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListCreationService;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListDataService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DownloadListController {

    private final DownloadListDataService downloadListDataService;
    private final DownloadListCreationService downloadListCreationService;
    private final SlskdRequestService slskdRequestService;

    @GetMapping(ApiRoutes.EP_PLAYLIST_DOWNLOADS)
    public ResponseEntity<List<DownloadListProcessDto>> getPlaylistDownloads(@PathVariable String playlistId){
        return ResponseEntity.ok(downloadListDataService.getDownloadListsProcessDataByPlaylistId(playlistId));
    }

    @GetMapping(ApiRoutes.EP_DOWNLOAD_LIST_TRACKS)
    public ResponseEntity<Page<SlskdRequestDto>> getDownloadListTracks(
            @PathVariable long downloadListId,
            @RequestParam(required = false) String processesByComa,
            @RequestParam(required = false) String nameContains,
            @PageableDefault(size = 10) Pageable pageable
    ){
        return ResponseEntity.ok(downloadListDataService
                .getDownloadListTracks(
                        downloadListId,
                        pageable,
                        processesByComa,
                        nameContains
                )
        );
    }

    @PostMapping(ApiRoutes.EP_DOWNLOAD_LIST)
    public ResponseEntity<Void> createDownloadList(@RequestBody DownloadListDto dto){
        downloadListCreationService.createNewDownloadListForExistingPlaylist(dto);
        return ResponseEntity
                .created(URI.create(""))
                .build();
    }

    @DeleteMapping(ApiRoutes.EP_DOWNLOAD_LIST +  "/{id}")
    public ResponseEntity<Void> deleteDownloadList(@PathVariable long id) {
        downloadListDataService.deleteDownloadListAndRequests(id);
        return ResponseEntity.ok(null);
    }

    @PostMapping(ApiRoutes.EP_DOWNLOAD_LIST_PAUSE)
    public ResponseEntity<Void> pauseDownloadList(@PathVariable long downloadListId){
        downloadListCreationService.pauseDownloadList(downloadListId);
        return ResponseEntity.ok(null);
    }

    @GetMapping(ApiRoutes.EP_DOWNLOAD_LIST_BY_POLICY)
    public ResponseEntity<List<DownloadListProcessDto>> getDownloadListsBySearchPolicy(@PathVariable String searchPolicyId) {
        return ResponseEntity.ok(downloadListDataService.getDownloadListsBySearchPolicy(searchPolicyId));
    }

    @PostMapping(ApiRoutes.EP_RESET_TRACK_DOWNLOAD)
    public ResponseEntity<Void> resetTrackDownload(@PathVariable long slskdReqId){
        slskdRequestService.resetRequest(slskdReqId);
        return ResponseEntity.ok(null);
    }

    @PostMapping(ApiRoutes.EP_MODIFY_TRACK_SEARCH_INPUT)
    public ResponseEntity<?> modifyTrackSearchInput(
            @PathVariable long slskdReqId,
            @RequestBody SlskdRequestDto requestDto
    ){
        slskdRequestService.modifySearchInput(slskdReqId, requestDto.searchInput());
        return ResponseEntity.ok(null);
    }

}
