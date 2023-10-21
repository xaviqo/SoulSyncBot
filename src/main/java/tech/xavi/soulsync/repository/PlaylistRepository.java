package tech.xavi.soulsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.Playlist;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.songs s " +
            "WHERE p.updatable = true  AND s.found = false")
    List<Playlist> fetchUnfoundSongsInPlaylists();
    @Query("SELECT p.spotifyId FROM Playlist p")
    List<String> findAllSpotifyId();

}
