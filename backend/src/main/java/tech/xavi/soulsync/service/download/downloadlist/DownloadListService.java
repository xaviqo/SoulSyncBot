package tech.xavi.soulsync.service.download.downloadlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.repository.db.DownloadListRepository;

import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class DownloadListService {

    private final DownloadListRepository downloadListRepository;

    public DownloadList save(DownloadList downloadList) {
        return downloadListRepository.save(downloadList);
    }

    public Stream<DownloadList> getDownloadLists() {
        return downloadListRepository
                .findAll()
                .stream();
    }

    public Stream<DownloadList> getPlaylistDownloadLists(String playlistId){
        return downloadListRepository
                .findAllByPlaylistId(playlistId);
    }

    public Optional<DownloadList> findById(long id){
        return downloadListRepository.findById(id);
    }

    public void deleteById(long id){
        downloadListRepository.deleteByDownloadListId(id);
    }

    public Stream<DownloadList> findBySearchPolicy(String searchPolicyId) {
        return downloadListRepository.findAllBySearchPolicy(searchPolicyId);
    }

    public long countTotal(){
        return downloadListRepository.count();
    }

}
