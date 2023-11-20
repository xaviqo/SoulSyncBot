package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter @Setter
@Builder
public class Song {
    @Id @GeneratedValue
    int id;
    @Column
    String spotifyId;
    @Column
    String name;
    @Column
    String searchInput;
    @Column
    UUID searchId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "artists", joinColumns = @JoinColumn(name = "song_id"))
    @Column(name = "artist")
    List<String> artists;
    @Enumerated(EnumType.STRING)
    @Column
    SongStatus status;
    @Column
    String filename;
    @Column
    long size;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "playlist_id")
    Playlist playlist;
    @Builder.Default
    @Column
    int attempts = 1;

    public void addAttempt(){
        this.setAttempts(this.getAttempts()+1);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", spotifyId='" + spotifyId + '\'' +
                ", name='" + name + '\'' +
                ", searchInput='" + searchInput + '\'' +
                ", searchId=" + searchId +
                ", artists=" + artists +
                ", status=" + status +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                ", playlist=" + playlist.getSpotifyId() +
                ", attempts=" + attempts +
                '}';
    }
}