package tech.xavi.soulsync.dto.projection;

import tech.xavi.soulsync.entity.sub.PlaylistType;

public interface PlaylistProjection {
    String getSpotifyId();
    String getName();
    String getCover();
    int getLastTotalTracks();
    long getLastUpdate();
    long getAdded();
    PlaylistType getType();
    boolean isUpdatable();
    boolean isAvoidDuplicates();
}