package tech.xavi.soulsync.dto.gateway;

import lombok.Data;

@Data
public class SpotifyPlaylistTotalTracksResponse {

    private Tracks tracks;

    @Data
    public static class Tracks {
        private int total;
    }
}
