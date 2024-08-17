package tech.xavi.soulsync.entity.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class DownloadList {

    @Id
    private Long downloadListId;
    @Column(name = "playlist_id")
    private String playlistId;
    @Column
    private String searchPolicy;
    @Column
    private Boolean isActive;
    @Column @Enumerated(EnumType.STRING)
    private DownloadPriority priority;
    @Column
    private long attempts;
    @Column
    private long lastCheck;

    // lower num is higher priority
    public long getPriorityRank() {
        long priorityMultiplier = this.priority.ordinal() + 1;
        long retriesMultiplier = this.attempts + 1;
        return priorityMultiplier * retriesMultiplier;
    }

    public void increaseAttempts() {
        this.attempts++;
    }

    @Override
    public String toString() {
        return "DownloadList{" +
                "downloadListId=" + downloadListId +
                ", searchPolicy='" + searchPolicy + '\'' +
                ", isActive=" + isActive +
                ", priority=" + priority +
                ", attempts=" + attempts +
                ", lastCheck=" + lastCheck +
                '}';
    }
}
