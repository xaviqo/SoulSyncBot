package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import tech.xavi.soulsync.configuration.globals.PlaylistType;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "spotifyId")
@Entity
@Getter
@Setter
@Builder
public class Playlist {

    @Id
    private String spotifyId;
    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private PlaylistType playlistType;
    @Column
    private String cover;
    @Column
    private String name;
    @Column
    private String owner;
    @Column
    private int totalTracks;
    @Column
    private boolean shouldRenameRelocated;
    @Column
    private long lastUpdate;
    @ManyToMany(fetch = FetchType.LAZY) @Builder.Default
    private Set<SpotifySong> songs = new HashSet<>();
    @ManyToMany @Builder.Default
    private Set<SlskdDownload> downloads = new HashSet<>();

}
