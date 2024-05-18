package tech.xavi.soulsync.service.download.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.repository.db.DownloadListRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DownloadListService {

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

    @Transactional(readOnly = true)
    public Set<DownloadList> getPlaylistDownloadLists(String playlistId){
        return downloadListRepository
                .findAllByPlaylistId(playlistId)
                .collect(Collectors.toSet());
    }

}
