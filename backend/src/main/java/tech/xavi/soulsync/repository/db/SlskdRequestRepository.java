package tech.xavi.soulsync.repository.db;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;

import java.util.Optional;
import java.util.Set;

public interface SlskdRequestRepository extends CrudRepository<SlskdRequest, Long> {

    Set<SlskdRequest> findByDownloadList(DownloadList downloadList);
    Page<SlskdRequest> findByDownloadList(DownloadList downloadList, Pageable pageable);
    Optional<SlskdRequest> findByFilenameAndSharedBy(String filename, String sharedBy);
    Set<SlskdRequest> findByStatus(ProcessStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE SlskdRequest s SET s.status = :status WHERE s.filename = :filename AND s.sharedBy = :username")
    void updateStatusByFileAndUser(ProcessStatus status, String filename, String username);

    long countByDownloadList(DownloadList downloadList);

    @Query("SELECT COUNT(1) FROM SlskdRequest s WHERE s.downloadList = :downloadList AND s.status IN :statuses")
    long countByDownloadListAndStatuses(DownloadList downloadList, ProcessStatus[] statuses);

}
