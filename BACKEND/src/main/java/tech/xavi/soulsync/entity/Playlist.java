package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import tech.xavi.soulsync.entity.sub.PlaylistType;

import java.util.HashSet;
import java.util.Set;

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
    @Enumerated(EnumType.STRING)
    @Column
    PlaylistType type;
    @Column
    int lastTotalTracks;
    @Column
    long lastUpdate;
    @Column
    boolean updatable;
    @Column
    long added;
    @Column
    int releaseYear;
    @JsonManagedReference
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    Set<Song> songs = new HashSet<>();

    public void addSong(Song song){
        this.songs.add(song);
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
