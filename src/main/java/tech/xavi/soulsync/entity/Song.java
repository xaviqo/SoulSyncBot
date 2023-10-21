package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
public class Song {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
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
    @Column
    boolean found;
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

}
