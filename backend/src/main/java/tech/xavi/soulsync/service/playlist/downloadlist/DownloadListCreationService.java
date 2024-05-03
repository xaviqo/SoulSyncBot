package tech.xavi.soulsync.service.playlist.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.entity.datafile.DownloadList;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.entity.db.SlskdDownload;
import tech.xavi.soulsync.service.search.SearchInputService;
import tech.xavi.soulsync.service.search.SearchPolicyMainService;
import tech.xavi.soulsync.service.song.SlskdDownloadsService;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DownloadListCreationService {

    private final SlskdDownloadsService slskdDownloadsService;
    private final DownloadListMainService downloadListMainService;
    private final SearchPolicyMainService searchPolicyMainService;
    private final SearchInputService searchInputService;

    public DownloadList createDownloadList(String playlistId, String searchPolicyId) {
        return downloadListMainService.save(
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
        SearchPolicy searchPolicy = searchPolicyMainService
                .getPolicyById(downloadList.getSearchPolicy());

        Set<SlskdDownload> slskdDownloads = playlist
                .getSongs()
                .stream()
                .parallel()
                .map( song -> SlskdDownload.builder()
                        .downloadList(downloadList)
                        .spotifySong(song)
                        .playlist(playlist)
                        .searchInput(searchInputService.getSearchInput(song,searchPolicy))
                        .attempts(0L)
                        .added(System.currentTimeMillis())
                        .build())
                .collect(Collectors.toSet());

        slskdDownloadsService
                .saveAll(slskdDownloads);
    }
}
