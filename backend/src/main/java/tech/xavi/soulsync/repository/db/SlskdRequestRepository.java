package tech.xavi.soulsync.repository.db;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.stats.StatusCountDto;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SlskdRequestRepository extends CrudRepository<SlskdRequest, Long> {

    Set<SlskdRequest> findByDownloadList(DownloadList downloadList);
    Page<SlskdRequest> findByDownloadListAndStatusIn(DownloadList downloadList, List<ProcessStatus> statusList, Pageable pageable);
    Page<SlskdRequest> findByDownloadListAndStatusInAndSearchInputContaining(DownloadList downloadList, List<ProcessStatus> statusList, String searchInput, Pageable pageable);
    Optional<SlskdRequest> findByFilenameAndSharedBy(String filename, String sharedBy);
    Set<SlskdRequest> findByStatus(ProcessStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE SlskdRequest s SET s.status = :status WHERE s.filename = :filename AND s.sharedBy = :username")
    void updateStatusByFileAndUser(ProcessStatus status, String filename, String username);

    long countByDownloadList(DownloadList downloadList);

    @Query("SELECT COUNT(1) FROM SlskdRequest s WHERE s.status IN :statuses AND s.attempts >= :minAttempts")
    long countByStatuses(ProcessStatus[] statuses, long minAttempts);

    @Query("SELECT COUNT(1) FROM SlskdRequest s WHERE s.downloadList = :downloadList AND s.status IN :statuses AND s.attempts >= :minAttempts")
    long countByDownloadListAndStatuses(DownloadList downloadList, ProcessStatus[] statuses, long minAttempts);

    @Query("SELECT new tech.xavi.soulsync.dto.stats.StatusCountDto(sr.status, COUNT(sr)) FROM SlskdRequest sr GROUP BY sr.status")
    List<StatusCountDto> countByStatus();

    void deleteSlskdRequestByDownloadList(DownloadList downloadList);
}
