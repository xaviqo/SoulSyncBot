package tech.xavi.soulsync.model;

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
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "playlist_id")
    List<Song> songs;
    @Column
    int lastTotalTracks;
    @Column
    boolean scanning;
}
