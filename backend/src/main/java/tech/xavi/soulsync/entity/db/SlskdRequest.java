package tech.xavi.soulsync.entity.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Getter
@Setter
@Builder
public class SlskdRequest {

    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumn(name = "download_list_id")
    private DownloadList downloadList;
    @ManyToOne
    private SpotifySong spotifySong;
    @ManyToOne
    private Playlist playlist;
    @Column(nullable = false)
    private String searchInput;
    @Column
    private UUID searchId;
    @Enumerated(EnumType.STRING)
    @Column
    private ProcessStatus status;
    @Column
    private long attempts;
    @Column
    private long lastCheck;
    @Column
    private long added;
    @Column
    private String copyRoute;
    @Column
    private String filename;
    @Column
    private long size;
    @Column
    private int bitRate;
    @Column
    private String sharedBy;

    public void increaseAttempts() {
        this.attempts++;
    }

    public String getArtistsNames() {
        SpotifySong song = getSpotifySong();
        if (song != null) {
            List<Artist> artists = song.getArtists();
            if (artists != null && !artists.isEmpty()) {
                return artists.stream()
                        .map(Artist::getName)
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.joining(", ")) + " - ";
            }
        }
        return "";
    }


    @Override
    public String toString() {
        return "SlskdRequest{" +
                "id=" + id +
                ", searchInput='" + searchInput + '\'' +
                ", spotifySong=" + spotifySong +
                '}';
    }
}
