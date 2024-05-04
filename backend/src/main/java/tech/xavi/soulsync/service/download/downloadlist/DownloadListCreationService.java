package tech.xavi.soulsync.service.download.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.search.SearchInputService;
import tech.xavi.soulsync.service.search.SearchPolicyService;
import tech.xavi.soulsync.service.download.SlskdRequestService;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DownloadListCreationService {

    private final SlskdRequestService slskdRequestService;
    private final DownloadListService downloadListService;
    private final SearchPolicyService searchPolicyService;
    private final SearchInputService searchInputService;

    public DownloadList createDownloadList(String playlistId, String searchPolicyId) {
        return downloadListService.save(
                DownloadList.builder()
                        .playlistId(playlistId)
                        .searchPolicy(searchPolicyId)
                        .isActive(true)
                        .priority(DownloadPriority.NORMAL)
                        .attempts(0L)
                        .build()
        );
    }

    public void createSlskdDownloads(
            Playlist playlist,
            DownloadList downloadList
    ) {
        SearchPolicy searchPolicy = searchPolicyService
                .getPolicyById(downloadList.getSearchPolicy());

        Set<SlskdRequest> slskdRequests = playlist
                .getSongs()
                .stream()
                .parallel()
                .map( song -> SlskdRequest.builder()
                        .downloadList(downloadList)
                        .spotifySong(song)
                        .playlist(playlist)
                        .searchInput(searchInputService.getSearchInput(song,searchPolicy))
                        .attempts(0L)
                        .added(System.currentTimeMillis())
                        .build())
                .collect(Collectors.toSet());

        slskdRequestService
                .saveAll(slskdRequests);
    }
}
