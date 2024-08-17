package tech.xavi.soulsync.service.download.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.downloadlist.DownloadListDto;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.playlist.PlaylistService;
import tech.xavi.soulsync.service.search.SearchInputService;
import tech.xavi.soulsync.service.search.SearchPolicyService;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DownloadListCreationService {

    private final SlskdRequestService slskdRequestService;
    private final DownloadListService downloadListService;
    private final SearchPolicyService searchPolicyService;
    private final SearchInputService searchInputService;
    private final PlaylistService playlistService;

    public void createNewDownloadListForExistingPlaylist(DownloadListDto dto) {
        Playlist playlist = playlistService
                .findById(dto.getPlaylistId())
                .orElseThrow(() -> new SoulSyncException(
                        SoulSyncError.PLAYLIST_NOT_FOUND,
                        HttpStatus.NOT_FOUND)
                );

        SearchPolicy searchPolicy = isNewDownloadList(dto)
                ? searchPolicyService
                .upsert(dto.getNewSearchPolicy())
                : searchPolicyService
                .getPolicyByRequest(dto.getSearchPolicy());

        if (Objects.isNull(searchPolicy))
            throw new SoulSyncException(SoulSyncError.SEARCH_POLICY_NOT_FOUND, HttpStatus.NOT_FOUND);

        DownloadList downloadList = downloadListService.save(DownloadList.builder()
                .downloadListId(System.currentTimeMillis())
                .playlistId(dto.getPlaylistId())
                .searchPolicy(searchPolicy.getId())
                .isActive(true)
                .priority(searchPolicy.getDownloadPriority())
                .attempts(0)
                .lastCheck(0)
                .build());

        createSlskdRequests(playlist, downloadList);
    }

    public synchronized DownloadList createDownloadListForNewPlaylist(String playlistId, String searchPolicyId) {
        SearchPolicy policy = searchPolicyService.getPolicyByRequest(searchPolicyId);
        return DownloadList.builder()
                .downloadListId(System.currentTimeMillis())
                .playlistId(playlistId)
                .searchPolicy(policy.getId())
                .isActive(true)
                .priority(policy.getDownloadPriority())
                .attempts(0L)
                .build();
    }

    public void createSlskdRequests(
            Playlist playlist,
            DownloadList downloadList
    ) {
        SearchPolicy searchPolicy = searchPolicyService
                .getPolicyByRequest(downloadList.getSearchPolicy());

        Set<SlskdRequest> slskdRequests = playlist
                .getSongs()
                .stream()
                .parallel()
                .map( song -> SlskdRequest.builder()
                        .searchId(UUID.randomUUID())
                        .downloadList(downloadList)
                        .spotifySong(song)
                        .playlist(playlist)
                        .searchInput(searchInputService.getSearchInput(song,searchPolicy))
                        .attempts(0L)
                        .added(System.currentTimeMillis())
                        .status(ProcessStatus.WAITING)
                        .build())
                .collect(Collectors.toSet());

        slskdRequestService
                .saveAll(slskdRequests);
    }

    public void pauseDownloadList(long downloadListId){
        downloadListService
                .findById(downloadListId)
                .ifPresent( dl -> {
                    dl.setIsActive(!dl.getIsActive());
                    downloadListService.save(dl);
                });
    }

    private boolean isNewDownloadList(DownloadList downloadList) {
        return Objects.isNull(downloadList.getDownloadListId())
                && Objects.isNull(downloadList.getSearchPolicy());
    }
}
