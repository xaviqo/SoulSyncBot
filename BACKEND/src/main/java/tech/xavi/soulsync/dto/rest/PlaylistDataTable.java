package tech.xavi.soulsync.dto.rest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaylistDataTable {
    String id;
    String name;
    String cover;
    long lastUpdate;
    int total;
    int totalSucceeded;
}
