package tech.xavi.soulsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.Song;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Integer> {

    @Query("SELECT s FROM Song s WHERE s.playlist.spotifyId = :id")
    List<Song> getSongsByPlaylistId(@Param("id") String spotifyId);

    Song findByFilename(String filename);

    @Query("SELECT count(1) FROM Song s WHERE s.found = false AND s.playlist.spotifyId = :id")
    Integer getTotalUnfoundSongsFromPlaylist(@Param("id") String spotifyId);
}
