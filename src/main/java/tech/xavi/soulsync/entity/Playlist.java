package tech.xavi.soulsync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter @Setter
@Builder
public class Playlist{
    @Id
    String spotifyId;
    @Column
    String name;
    @Column
    String cover;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "playlist_id")
    List<Song> songs;
    @Column
    int lastTotalTracks;
    @Column
    long lastUpdate;
    @Column
    boolean updatable;
    @Column
    boolean avoidDuplicates;

    @Override
    public String toString() {
        return "Playlist{" +
                "spotifyId='" + spotifyId + '\'' +
                ", name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", lastTotalTracks=" + lastTotalTracks +
                ", lastUpdate=" + lastUpdate +
                ", updatable=" + updatable +
                ", avoidDuplicates=" + avoidDuplicates +
                '}';
    }
}
