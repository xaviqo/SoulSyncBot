package tech.xavi.soulsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.Song;

import java.util.UUID;

@Repository
public interface SongRepository extends JpaRepository<Song, Integer> {
    Song findBySearchId(UUID id);
}
