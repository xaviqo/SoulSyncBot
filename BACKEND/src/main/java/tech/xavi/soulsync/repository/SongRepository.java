package tech.xavi.soulsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.SongStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SongRepository extends JpaRepository<Song, Integer> {

    Optional<Set<Song>> findByStatus(SongStatus status);

    Optional<Song> findBySpotifyId(String spotifyId);

    @Query("FROM Song s WHERE s.filename = :filename")
    List<Song> findSongsByFilename(@Param("filename") String filename);

    @Query("SELECT count(1) FROM PlaylistSongRelation pls " +
            "WHERE pls.playlist.spotifyId = :id " +
            "AND pls.song.status = :status")
    Integer countSongsByStatusByPlaylistId(
            @Param("id") String spotifyId,
            @Param("status") SongStatus status
    );

}
