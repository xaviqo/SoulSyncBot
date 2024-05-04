package tech.xavi.soulsync.service.download;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.repository.db.SlskdRequestRepository;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class SlskdRequestService {

    private final SlskdRequestRepository slskdRequestRepository;

    public Stream<SlskdRequest> getSongsQueue(DownloadList downloadList) {
        long retiesThreshold = downloadList.getAttempts();
        return getDownloadListSongs(downloadList)
                .stream()
                .filter( song -> song.getAttempts() <= retiesThreshold);
    }

    public void saveIncreasingAttempts(SlskdRequest slskdRequest) {
        slskdRequest.increaseAttempts();
        save(slskdRequest);
    }

    public SlskdRequest save(SlskdRequest slskdRequest) {
        return slskdRequestRepository.save(slskdRequest);
    }

    public Iterable<SlskdRequest> saveAll(Collection<SlskdRequest> slskdRequests) {
        return slskdRequestRepository.saveAll(slskdRequests);
    }

    private Set<SlskdRequest> getDownloadListSongs(DownloadList downloadList) {
        return slskdRequestRepository.findByDownloadList(downloadList);
    }

}
