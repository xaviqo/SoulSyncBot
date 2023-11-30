package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter @Setter
@Builder
public class Playlist{

    @Id @Column
    String spotifyId;
    @Column
    String name;
    @Column
    String cover;
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "playlist", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<PlaylistSongRelation> songsRelation = new HashSet<>();
    @Column
    int lastTotalTracks;
    @Column
    long lastUpdate;
    @Column
    boolean updatable;
    @Column
    long added;
    public Set<Song> getSongs(){
        return songsRelation.stream()
                .map(PlaylistSongRelation::getSong)
                .collect(Collectors.toSet());
    }

    public void addSong(Song song){
        PlaylistSongRelationId relationId = PlaylistSongRelationId.builder()
                .songSpotifyId(song.getSpotifyId())
                .plSpotifyId(this.getSpotifyId())
                .build();
        PlaylistSongRelation relation = PlaylistSongRelation.builder()
                .relationId(relationId)
                .playlist(this)
                .song(song)
                .build();
        songsRelation.add(relation);
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "spotifyId='" + spotifyId + '\'' +
                ", name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", lastTotalTracks=" + lastTotalTracks +
                ", lastUpdate=" + lastUpdate +
                ", updatable=" + updatable +
                '}';
    }
}
