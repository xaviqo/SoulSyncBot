package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import tech.xavi.soulsync.configuration.constants.ConfigurationFinals;
import tech.xavi.soulsync.entity.sub.SongStatus;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter @Setter
@Builder
public class Song {

    @Id @Column
    String spotifyId;
    @Column
    String name;
    @Column
    String artists;
    @Column
    String album;
    @Column
    String searchInput;
    @Column
    UUID searchId;
    @Enumerated(EnumType.STRING)
    @Column
    SongStatus status;
    @Column
    String filename;
    @Column
    long size;
    @Column
    int attempts;
    @Column
    long lastCheck;
    @Column
    long added;
    @Column
    String copyRoute;
    @JsonBackReference
    @Builder.Default
    @ManyToMany(mappedBy = "songs")
    Set<Playlist> playlists = new HashSet<>();

    public void addAttempt(){
        this.setAttempts(this.getAttempts()+1);
    }

    public String[] getArtists(){
        return artists.split(ConfigurationFinals.ARTIST_DIVIDER);
    }

    @Override
    public String toString() {
        return "Song{" +
                " spotifyId='" + spotifyId + '\'' +
                ", name='" + name + '\'' +
                ", searchInput='" + searchInput + '\'' +
                ", searchId=" + searchId +
                ", artists=" + artists +
                ", status=" + status +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                ", attempts=" + attempts +
                ", lastCheck=" + lastCheck +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(
                spotifyId,
                ((Song) o).spotifyId
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(spotifyId);
    }

}
