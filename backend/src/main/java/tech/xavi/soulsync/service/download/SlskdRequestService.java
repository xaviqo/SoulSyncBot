package tech.xavi.soulsync.service.download;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdFile;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.repository.db.SlskdRequestRepository;
import tech.xavi.soulsync.service.integration.SlskdGatewayService;

import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class SlskdRequestService {

    private final SlskdRequestRepository slskdRequestRepository;
    private final SlskdGatewayService slskdGatewayService;

    private static final String SUCCESS_STATUS = "Completed, Succeeded";
    private static final String[] STUCK_STATUS = { "Queued, remotely","Completed, Cancelled","Completed, Errored" };

    public Stream<SlskdFile> getAllStuckDownloads() {
        return slskdGatewayService
                .getSlskdDownloads()
                .flatMap( dws -> dws.directories()
                        .stream()
                        .flatMap( dir -> filterByStatus(dir.files(),STUCK_STATUS) )
                );
    }

    public Stream<SlskdFile> getCompletedDownloads() {
        return slskdGatewayService
                .getSlskdDownloads()
                .flatMap( dws -> dws.directories()
                        .stream()
                        .flatMap( dir -> filterByStatus(dir.files(),SUCCESS_STATUS) )
                );
    }

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

    public void setRequestToWaiting(SlskdRequest slskdRequest) {
        slskdRequest.setStatus(ProcessStatus.WAITING);
        slskdRequest.setSharedBy(null);
        slskdRequest.setFilename(null);
        slskdRequest.setSize(0);
        slskdRequest.setBitRate(0);
        save(slskdRequest);
    }

    public Optional<SlskdRequest> findByFilenameAndUser(SlskdFile slskdFile) {
        return slskdRequestRepository.findByFilenameAndSharedBy(slskdFile.filename(), slskdFile.username());
    }

    public void setRequestToCompletedByFile(SlskdFile slskdFile) {
        slskdRequestRepository
                .updateStatusByFileAndUser(
                        ProcessStatus.COMPLETED,
                        slskdFile.filename(),
                        slskdFile.username()
                );
    }

    public SlskdRequest save(SlskdRequest slskdRequest) {
        return slskdRequestRepository.save(slskdRequest);
    }

    public void sendDownloadRequest(SlskdRequest request) {
        slskdGatewayService.initDownload(request);
    }

    public Iterable<SlskdRequest> saveAll(Collection<SlskdRequest> slskdRequests) {
        return slskdRequestRepository.saveAll(slskdRequests);
    }

    public long countByDownloadList(DownloadList downloadList) {
        return slskdRequestRepository.countByDownloadList(downloadList);
    }

    public long countByDownloadListAndStatus(DownloadList downloadList, ProcessStatus... status) {
        return slskdRequestRepository.countByDownloadListAndStatuses(downloadList, status);
    }

    public Page<SlskdRequest> getDownloadListSongs(long downloadListId, Pageable pageable){
        DownloadList dl = DownloadList.builder().downloadListId(downloadListId).build();
        return slskdRequestRepository.findByDownloadList(dl,pageable);
    }

    public Set<SlskdRequest> getDownloadListSongs(DownloadList downloadList) {
        return slskdRequestRepository.findByDownloadList(downloadList);
    }

    private Stream<SlskdFile> filterByStatus(List<SlskdFile> files, String... status) {
        return files
                .stream()
                .filter(file ->
                        Arrays.stream(status)
                                .anyMatch(s -> s.equals(file.state()) )
                );
    }

}
