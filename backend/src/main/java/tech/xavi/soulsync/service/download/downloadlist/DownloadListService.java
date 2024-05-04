package tech.xavi.soulsync.service.download.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.repository.db.DownloadListRepository;
import tech.xavi.soulsync.service.search.SearchPolicyService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DownloadListService {

    private final SearchPolicyService searchPolicyService;
    private final DownloadListRepository downloadListRepository;

    public DownloadList save(DownloadList downloadList) {
        return downloadListRepository.save(downloadList);
    }

    public Optional<DownloadList> getNextDownloadList() {
        return downloadListRepository
                .findAll()
                .stream()
                .filter(DownloadList::getIsActive)
                .min(DownloadPriority::compare);
    }

    //private DownloadList get

    public SearchPolicy getDownloadListSearchPolicy(String searchPolicyId) {
        return searchPolicyService.getPolicyById(searchPolicyId);
    };

    private Optional<DownloadList> getDownloadListById(long id) {
        return downloadListRepository.findById(id);
    }

}
