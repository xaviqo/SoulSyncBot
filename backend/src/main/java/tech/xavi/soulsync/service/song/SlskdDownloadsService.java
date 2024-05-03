package tech.xavi.soulsync.service.song;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.datafile.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdDownload;
import tech.xavi.soulsync.repository.db.SlskdDownloadRepository;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class SlskdDownloadsService {

    private final SlskdDownloadRepository slskdDownloadRepository;

    public Stream<SlskdDownload> getSongsQueue(DownloadList downloadList) {
        long retiesThreshold = downloadList.getAttempts();
        return getDownloadListSongs(downloadList)
                .stream()
                .filter( song -> song.getAttempts() <= retiesThreshold);
    }

    public void saveIncreasingAttempts(Collection<SlskdDownload> slskdDownloads) {
        Collection<SlskdDownload> updatedDownloads = slskdDownloads
                .stream()
                .peek(SlskdDownload::increaseAttempts)
                .collect(Collectors.toSet());
        saveAll(updatedDownloads);
    }

    public SlskdDownload save(SlskdDownload slskdDownload) {
        return slskdDownloadRepository.save(slskdDownload);
    }

    public Iterable<SlskdDownload> saveAll(Collection<SlskdDownload> slskdDownloads) {
        return slskdDownloadRepository.saveAll(slskdDownloads);
    }

    private Set<SlskdDownload> getDownloadListSongs(DownloadList downloadList) {
        return slskdDownloadRepository.findByDownloadList(downloadList);
    }
}
