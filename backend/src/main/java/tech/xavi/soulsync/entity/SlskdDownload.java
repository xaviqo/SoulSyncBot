package tech.xavi.soulsync.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import tech.xavi.soulsync.configuration.globals.DownloadStatus;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Getter
@Setter
@Builder
public class SlskdDownload {

    @Id @Column
    private long id;
    @ManyToOne
    private SpotifySong spotifySong;
    @ManyToOne
    private Playlist playlist;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String artists;
    @Column(nullable = false)
    private String searchInput;
    @Column(nullable = false)
    private UUID searchId;
    @Enumerated(EnumType.STRING)
    @Column
    private DownloadStatus status;
    @Column
    private String filename;
    @Column
    private long size;
    @Column
    private int attempts;
    @Column
    private long lastCheck;
    @Column
    private long added;
    @Column
    private String copyRoute;

}
