package tech.xavi.soulsync.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.xavi.soulsync.entity.db.DownloadList;

import java.util.stream.Stream;

@Repository
public interface DownloadListRepository extends JpaRepository<DownloadList, Long> {
    Stream<DownloadList> findAllByPlaylistId(String playlistId);
    void deleteByDownloadListId(long id);
    Stream<DownloadList> findAllBySearchPolicy(String searchPolicy);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM playlist_download_lists WHERE download_lists_download_list_id = :downloadListId", nativeQuery = true)
    void deleteFromPlaylistDownloadListsByDownloadListId(Long downloadListId);
}
