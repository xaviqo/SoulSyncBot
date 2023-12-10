package tech.xavi.soulsync.dto.service;

import lombok.Builder;
import lombok.Data;
import tech.xavi.soulsync.entity.sub.PlaylistType;

@Data
@Builder
public class PlaylistDataTable {
    String id;
    String name;
    String cover;
    long added;
    long lastUpdate;
    int total;
    int totalSucceeded;
    PlaylistType type;
}
