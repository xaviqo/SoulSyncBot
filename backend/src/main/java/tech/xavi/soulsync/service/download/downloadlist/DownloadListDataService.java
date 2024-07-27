package tech.xavi.soulsync.service.download.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.downloadlist.DownloadListProcessDto;
import tech.xavi.soulsync.dto.playlist.SlskdRequestDto;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.service.download.SlskdRequestService;

import java.util.List;

@Service @RequiredArgsConstructor
public class DownloadListDataService {

    private final DownloadListService downloadListService;
    private final SlskdRequestService slskdRequestService;

    @Transactional(readOnly = true)
    public List<DownloadListProcessDto> getDownloadListsBySearchPolicy(String searchPolicyId) {
        return downloadListService
                .findBySearchPolicy(searchPolicyId)
                .map(this::mapToProcessDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DownloadListProcessDto> getDownloadListsProcessDataByPlaylistId(String playlistId) {
        return downloadListService.getPlaylistDownloadLists(playlistId)
                .map(this::mapToProcessDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SlskdRequestDto> getDownloadListTracks(long downloadListId, Pageable pageable){
        return slskdRequestService
                .getDownloadListSongs(downloadListId, pageable)
                .map( req -> SlskdRequestDto.builder()
                        .id(req.getId())
                        .name(req.getSpotifySong().getName())
                        .artists(req.getSpotifySong().getArtists())
                        .album(req.getSpotifySong().getAlbum())
                        .searchInput(req.getSearchInput())
                        .status(req.getStatus())
                        .attempts(req.getAttempts())
                        .lastCheck(req.getLastCheck())
                        .added(req.getAdded())
                        .copyRoute(req.getCopyRoute())
                        .filename(req.getFilename())
                        .size(req.getSize())
                        .bitRate(req.getBitRate())
                        .sharedBy(req.getSharedBy())
                        .build() );
    }

    @Transactional
    public void deleteDownloadListAndRequests(long downloadListId) {
        DownloadList downloadList = DownloadList.builder().downloadListId(downloadListId).build();
        downloadListService.deletePlaylistDownloadLists(downloadListId);
        slskdRequestService.deleteDownloadListsSlskdRequests(downloadList);
        downloadListService.deleteById(downloadListId);
    }

    private DownloadListProcessDto mapToProcessDto(DownloadList list) {
        long totalTracks = slskdRequestService
                .countByDownloadList(list);
        long totalCompleted = slskdRequestService
                .countByDownloadListAndStatus(list, ProcessStatus.COMPLETED, ProcessStatus.COPIED);
        return DownloadListProcessDto.builder()
                .id(list.getDownloadListId())
                .playlistId(list.getPlaylistId())
                .policyId(list.getSearchPolicy())
                .isActive(list.getIsActive())
                .priority(list.getPriority())
                .attempts(list.getAttempts())
                .lastCheck(list.getLastCheck())
                .totalTracks(totalTracks)
                .totalCompleted(totalCompleted)
                .build();
    }

}
