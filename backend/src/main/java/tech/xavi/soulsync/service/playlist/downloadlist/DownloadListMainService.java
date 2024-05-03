package tech.xavi.soulsync.service.playlist.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.entity.datafile.DownloadList;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.repository.db.DownloadListRepository;
import tech.xavi.soulsync.service.search.SearchPolicyMainService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DownloadListMainService {

    private final SearchPolicyMainService searchPolicyMainService;
    private final DownloadListRepository downloadListRepository;

    public DownloadList save(DownloadList downloadList) {
        return downloadListRepository.save(downloadList);
    }

    public DownloadList getNextDownloadList() {
        return downloadListRepository
                .findAll()
                .stream()
                .min(DownloadPriority::compare)
                .orElseThrow();
    }

    //private DownloadList get

    public SearchPolicy getDownloadListSearchPolicy(String searchPolicyId) {
        return searchPolicyMainService.getPolicyById(searchPolicyId);
    };

    private Optional<DownloadList> getDownloadListById(long id) {
        return downloadListRepository.findById(id);
    }

}
