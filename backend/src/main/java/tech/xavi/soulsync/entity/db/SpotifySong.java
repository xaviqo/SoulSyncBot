package tech.xavi.soulsync.entity.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "spotifyId")
@Entity
@Getter
@Setter
@Builder
public class SpotifySong {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Unique
    private String spotifyId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String album;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private List<Artist> artists = new ArrayList<>();
    @OneToMany
    private Set<SlskdRequest> slskdRequests = new HashSet<>();
    @JsonIgnore
    @ManyToMany(mappedBy = "songs", fetch = FetchType.LAZY)
    private Set<Playlist> playlists = new HashSet<>();

    public String getFirstArtistName() {
        return !artists.isEmpty()
                ? getArtists().getFirst().getName()
                : getAlbum();
    }

}
