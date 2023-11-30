package tech.xavi.soulsync.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class PlaylistSongRelationId implements Serializable {
    @Column
    String plSpotifyId;
    @Column
    String songSpotifyId;
}
