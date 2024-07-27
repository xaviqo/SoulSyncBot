package tech.xavi.soulsync.repository.db;


import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import tech.xavi.soulsync.entity.db.SpotifySong;

public interface SongRepository extends CrudRepository<SpotifySong,String> {
    Page<SpotifySong> findByPlaylistsId(String playlistId, Pageable pageable);
    @Modifying
    @Transactional
    @Query("DELETE FROM SpotifySong s WHERE s.spotifyId IN (" +
            "SELECT s.spotifyId FROM SpotifySong s WHERE s.spotifyId NOT IN (" +
            "SELECT sp.spotifyId FROM Playlist p JOIN p.songs sp))")
    void deleteOrphans();
}
