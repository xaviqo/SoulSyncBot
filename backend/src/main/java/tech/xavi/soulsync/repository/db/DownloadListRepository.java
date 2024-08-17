package tech.xavi.soulsync.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.db.DownloadList;

import java.util.stream.Stream;

@Repository
public interface DownloadListRepository extends JpaRepository<DownloadList, Long> {
    Stream<DownloadList> findAllByPlaylistId(String playlistId);
    void deleteByDownloadListId(long id);
    Stream<DownloadList> findAllBySearchPolicy(String searchPolicy);
}
