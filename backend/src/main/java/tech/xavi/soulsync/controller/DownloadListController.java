package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.playlist.DownloadListProcessDto;
import tech.xavi.soulsync.dto.playlist.SlskdRequestDto;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListDataService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DownloadListController {

    private final DownloadListDataService downloadListDataService;

    @GetMapping(ApiRoutes.EP_PLAYLIST_DOWNLOADS)
    public ResponseEntity<List<DownloadListProcessDto>> getPlaylistDownloads(@PathVariable String playlistId){
        return ResponseEntity.ok(downloadListDataService.getDownloadListsProcessDataByPlaylistId(playlistId));
    }

    @GetMapping(ApiRoutes.EP_DOWNLOAD_LIST_TRACKS)
    public ResponseEntity<Page<SlskdRequestDto>> getDownloadListTracks(@PathVariable long downloadListId, Pageable pageable){
        return ResponseEntity.ok(downloadListDataService.getDownloadListTracks(downloadListId,pageable));
    }



}
