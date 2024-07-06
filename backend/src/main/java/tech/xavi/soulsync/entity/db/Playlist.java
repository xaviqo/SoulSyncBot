package tech.xavi.soulsync.entity.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import tech.xavi.soulsync.configuration.globals.PlaylistType;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Getter
@Setter
@Builder
public class Playlist {

    @Id
    private String id;
    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private PlaylistType playlistType;
    @Column
    private String cover;
    @Column
    private String name;
    @Column
    private String owner;
    @Column
    private Integer totalTracks;
    @Column
    private Boolean shouldRenameRelocated;
    @Column
    private Boolean isUpdatable;
    @Column
    private Long lastUpdate;
    @ManyToMany(fetch = FetchType.LAZY) @Builder.Default
    private Set<SpotifySong> songs = new HashSet<>();
    @OneToMany @Builder.Default
    private Set<DownloadList> downloadLists = new HashSet<>();
    @OneToMany(mappedBy = "parentPlaylist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Playlist> subPlaylists = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Playlist parentPlaylist;

}
