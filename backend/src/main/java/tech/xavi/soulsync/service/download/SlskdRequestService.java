package tech.xavi.soulsync.service.download;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdFile;
import tech.xavi.soulsync.dto.stats.StatusCountDto;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.repository.db.SlskdRequestRepository;
import tech.xavi.soulsync.service.integration.SlskdGatewayService;

import java.util.*;
import java.util.stream.Collectors;
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

    public Stream<SlskdFile> getSlskdCompletedDownloads() {
        return slskdGatewayService
                .getSlskdDownloads()
                .flatMap( dws -> dws.directories()
                        .stream()
                        .flatMap( dir -> filterByStatus(dir.files(),SUCCESS_STATUS) )
                );
    }

    public Stream<SlskdRequest> getSongsQueue(DownloadList downloadList) {
        long retiesThreshold = downloadList.getAttempts();
        return getDownloadListTracks(downloadList)
                .stream()
                .filter( song -> song.getAttempts() <= retiesThreshold);
    }

    public Set<SlskdRequest> findByStatus(ProcessStatus status){
        return slskdRequestRepository.findByStatus(status);
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

    public List<StatusCountDto> countByStatus() {
        return slskdRequestRepository.countByStatus();
    }

    public long countByDownloadListAndStatus(DownloadList downloadList, long minAttempts, ProcessStatus... status) {
        return slskdRequestRepository.countByDownloadListAndStatuses(downloadList, status, minAttempts);
    }

    public long countByStatuses(long minAttempts, ProcessStatus... processStatus) {
        return slskdRequestRepository.countByStatuses(processStatus, minAttempts);
    }

    public Page<SlskdRequest> getDownloadListTracks(
            long downloadListId,
            String processesByComa,
            String nameContains,
            Pageable pageable
    ){
        DownloadList dl = DownloadList.builder()
                .downloadListId(downloadListId)
                .build();
        List<ProcessStatus> processArr = Arrays.stream(processesByComa.split(","))
                .map(ProcessStatus::valueOf)
                .collect(Collectors.toList());

        if (nameContains == null || nameContains.isEmpty())
            return slskdRequestRepository
                    .findByDownloadListAndStatusIn(
                            dl,
                            processArr,
                            PageRequest.of(
                                    pageable.getPageNumber(),
                                    pageable.getPageSize(),
                                    Sort.by("spotifySong.name").ascending()
                            )
                    );
        else
            return slskdRequestRepository
                    .findByDownloadListAndStatusInAndSearchInputContaining(
                            dl,
                            processArr,
                            nameContains,
                            PageRequest.of(
                                    pageable.getPageNumber(),
                                    pageable.getPageSize(),
                                    Sort.by("spotifySong.name").ascending()
                            )
                    );
    }

    public Set<SlskdRequest> getDownloadListTracks(DownloadList downloadList) {
        return slskdRequestRepository.findByDownloadList(downloadList);
    }

    public void deleteDownloadListsSlskdRequests(DownloadList downloadList) {
        slskdRequestRepository.deleteSlskdRequestByDownloadList(downloadList);
    }

    public void resetRequest(long id) {
        slskdRequestRepository
                .findById(id)
                .ifPresent(this::setRequestToWaiting);
    }

    public void modifySearchInput(long id, String searchInput) {
        slskdRequestRepository
                .findById(id)
                .ifPresent( request -> {
                    request.setSearchInput(searchInput);
                    save(request);
                });
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
