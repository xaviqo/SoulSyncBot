package tech.xavi.soulsync.repository.db;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import tech.xavi.soulsync.entity.db.SpotifySong;

public interface SongRepository extends CrudRepository<SpotifySong,String> {
    Page<SpotifySong> findByPlaylistsId(String playlistId, Pageable pageable);
}
