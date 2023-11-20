package tech.xavi.soulsync.dto.rest;

import lombok.Builder;
import lombok.Data;
import tech.xavi.soulsync.entity.PlaylistStatus;

@Data
@Builder
public class PlaylistDataTable {
    String id;
    String name;
    String cover;
    PlaylistStatus status;
    long lastUpdate;
    int total;
    int totalSucceeded;
}
