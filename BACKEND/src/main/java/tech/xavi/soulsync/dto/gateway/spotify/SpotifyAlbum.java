package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyAlbum {

    private String id;
    private AlbumTracksContainer tracks;
    private List<SpotifySong.Artist> artists;
    private Image[] images;
    private String name;
    @JsonProperty("total_tracks")
    private int totalTracks;
    @JsonProperty("release_date")
    private String releaseDate;

    public String getCover(){
        return this.images[0].url;
    }
    public int getReleaseYear(){
        return Integer.parseInt(this.releaseDate.split("-")[0]);
    }

    @Data
    private static class Image {
        private String url;
    }
    @Data
    public static class AlbumTracksContainer {
        private List<AlbumTrack> items;
    }
    @Data
    public static class AlbumTrack {
        private List<SpotifySong.Artist> artists;
        private String id;
        private String name;
    }
}
