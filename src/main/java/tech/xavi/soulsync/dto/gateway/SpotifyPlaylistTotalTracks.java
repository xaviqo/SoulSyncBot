package tech.xavi.soulsync.dto.gateway;

import lombok.Data;

@Data
public class SpotifyPlaylistTotalTracks {

    private Tracks tracks;

    @Data
    public static class Tracks {
        private int total;
    }
}
