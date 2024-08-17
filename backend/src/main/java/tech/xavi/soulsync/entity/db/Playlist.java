package tech.xavi.soulsync.entity.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import tech.xavi.soulsync.configuration.globals.PlaylistType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private String ownerRole;
    @Column
    private Integer totalTracks;
    @Column
    private Boolean isUpdatable;
    @Column
    private Long lastUpdate;
    @ManyToMany(fetch = FetchType.LAZY) @Builder.Default
    private Set<SpotifySong> songs = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "playlist_id")
    @Builder.Default
    private Set<DownloadList> downloadLists = new HashSet<>();
    @OneToMany(mappedBy = "parentPlaylist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Playlist> subPlaylists = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Playlist parentPlaylist;

    @Override
    public String toString() {
        return "Playlist{" +
                "id='" + id + '\'' +
                ", playlistType=" + playlistType +
                ", cover='" + cover + '\'' +
                ", totalTracks=" + totalTracks +
                ", name='" + name + '\'' +
                ", isUpdatable=" + isUpdatable +
                ", lastUpdate=" + lastUpdate +
                ", downloadLists=" + downloadLists +
                ", subPlaylists=" + (subPlaylists.isEmpty() ? 0 : subPlaylists
                    .stream()
                    .map(pl -> "("+pl.getId()+" - "+pl.getName()+")")
                    .collect(Collectors.joining(", "))) +
                ", parentPlaylist=" + (parentPlaylist != null ? "("+parentPlaylist.getId()+" - "+parentPlaylist.getName()+")" : "n/a") +
                '}';
    }
}
