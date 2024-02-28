package tech.xavi.soulsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.dto.projection.PlaylistProjection;
import tech.xavi.soulsync.entity.Playlist;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {

    Optional<Playlist> findBySpotifyId(String spId);

    @Query("SELECT p.spotifyId as spotifyId " +
            "FROM Playlist p " +
            "WHERE p.updatable = true")
    List<PlaylistProjection> findAllUpdatables();

    @Query("SELECT " +
            "p.spotifyId as spotifyId, " +
            "p.name as name, " +
            "p.cover as cover, " +
            "p.type as type, " +
            "p.added as added, " +
            "p.lastTotalTracks as lastTotalTracks, " +
            "p.lastUpdate as lastUpdate, " +
            "p.updatable as updatable " +
            "FROM Playlist p")
    List<PlaylistProjection> fetchPlaylistsDatatable();

    @Query("SELECT COUNT(1) " +
            "FROM Playlist p " +
            "WHERE p.spotifyId = :playlistId")
    Integer playlistExists(@Param("playlistId") String playlistId);

    @Modifying
    @Query("DELETE From Playlist p WHERE p.added >= :stamp")
    int deleteByAdded(@Param("stamp") long stamp);

}
