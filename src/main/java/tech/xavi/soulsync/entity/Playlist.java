package tech.xavi.soulsync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
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
}
