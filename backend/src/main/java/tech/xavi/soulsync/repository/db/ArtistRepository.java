package tech.xavi.soulsync.repository.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.db.Artist;

@Repository
public interface ArtistRepository extends CrudRepository<Artist,String> {

}
