package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.HashSet;
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
    private Long queueId;
    @Unique
    private String spotifyId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String album;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Set<Artist> artists = new HashSet<>();
    @OneToMany
    private Set<SlskdDownload> slskdDownloads = new HashSet<>();

}
