package tech.xavi.soulsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.dto.projection.PlaylistProjection;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.SongStatus;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    @Query("SELECT p FROM Playlist p " +
            "LEFT JOIN FETCH p.songs s " +
            "WHERE p.updatable = true  " +
            "AND s.status = :status")
    List<Playlist> getPlaylistBySongStatus(@Param("status") SongStatus status);

    @Query("SELECT " +
            "p.spotifyId as spotifyId, " +
            "p.avoidDuplicates as avoidDuplicates " +
            "FROM Playlist p " +
            "WHERE p.updatable = true")
    List<PlaylistProjection> findAllUpdatables();

    @Query("SELECT " +
            "p.spotifyId as spotifyId, " +
            "p.name as name, " +
            "p.cover as cover, " +
            "p.lastTotalTracks as lastTotalTracks, " +
            "p.lastUpdate as lastUpdate, " +
            "p.updatable as updatable, " +
            "p.avoidDuplicates as avoidDuplicates " +
            "FROM Playlist p")
    List<PlaylistProjection> fetchPlaylistsDatatable();

    @Query("SELECT COUNT(1) " +
            "FROM Playlist p " +
            "WHERE p.spotifyId = :playlistId")
    Integer playlistExists(@Param("playlistId") String playlistId);

}
