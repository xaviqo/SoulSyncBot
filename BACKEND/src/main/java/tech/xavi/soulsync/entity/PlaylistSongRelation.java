package tech.xavi.soulsync.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
public class PlaylistSongRelation {

    @EmbeddedId
    PlaylistSongRelationId relationId;

    @ManyToOne
    @MapsId("plSpotifyId")
    Playlist playlist;

    @ManyToOne
    @MapsId("songSpotifyId")
    Song song;

    boolean copied;

}
