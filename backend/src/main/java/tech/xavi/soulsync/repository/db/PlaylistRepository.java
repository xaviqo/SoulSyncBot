package tech.xavi.soulsync.repository.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.entity.db.Playlist;

import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface PlaylistRepository extends CrudRepository<Playlist,String> {
    @Override
    Set<Playlist> findAll();
    Stream<Playlist> findAllByPlaylistType(PlaylistType type);
    Stream<Playlist> findAllByParentPlaylist(Playlist playlistId);
    boolean existsById(String id);
}
