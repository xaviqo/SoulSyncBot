package tech.xavi.soulsync.dto.projection;

public interface PlaylistProjection {
    String getSpotifyId();
    String getName();
    String getCover();
    int getLastTotalTracks();
    long getLastUpdate();
    boolean isUpdatable();
}