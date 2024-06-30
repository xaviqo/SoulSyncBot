package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.downloadlist.DownloadListDto;
import tech.xavi.soulsync.dto.downloadlist.DownloadListProcessDto;
import tech.xavi.soulsync.dto.playlist.SlskdRequestDto;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListCreationService;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListDataService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DownloadListController {

    private final DownloadListDataService downloadListDataService;
    private final DownloadListCreationService downloadListCreationService;

    @GetMapping(ApiRoutes.EP_PLAYLIST_DOWNLOADS)
    public ResponseEntity<List<DownloadListProcessDto>> getPlaylistDownloads(@PathVariable String playlistId){
        return ResponseEntity.ok(downloadListDataService.getDownloadListsProcessDataByPlaylistId(playlistId));
    }

    @GetMapping(ApiRoutes.EP_DOWNLOAD_LIST_TRACKS)
    public ResponseEntity<Page<SlskdRequestDto>> getDownloadListTracks(@PathVariable long downloadListId, Pageable pageable){
        return ResponseEntity.ok(downloadListDataService.getDownloadListTracks(downloadListId,pageable));
    }

    @PostMapping(ApiRoutes.EP_DOWNLOAD_LIST)
    public ResponseEntity<?> createDownloadList(@RequestBody DownloadListDto dto){
        downloadListCreationService.createNewDownloadListForExistingPlaylist(dto);
        return ResponseEntity
                .created(URI.create(""))
                .build();
    }

    @PostMapping(ApiRoutes.EP_DOWNLOAD_LIST_PAUSE)
    public ResponseEntity<Void> pauseDownloadList(@PathVariable long downloadListId){
        downloadListCreationService.pauseDownloadList(downloadListId);
        return ResponseEntity.ok(null);
    }

}
