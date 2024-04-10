package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistDto {

    private String id;
    private AlbumTracksContainer tracks;
    private List<SpotifySongDto.Artist> artists;
    private Image[] images;
    private String name;
    private long totalTracks;

    @JsonProperty("tracks")
    private void unpackNestedTracks(JsonNode tracksNode) {
        this.totalTracks = tracksNode.get("total").asLong();
    }
    public String getCover() {
        return this.images != null
                ? this.images[0].getUrl()
                : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AlbumTracksContainer getTracks() {
        return tracks;
    }

    public void setTracks(AlbumTracksContainer tracks) {
        this.tracks = tracks;
    }

    public List<SpotifySongDto.Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<SpotifySongDto.Artist> artists) {
        this.artists = artists;
    }

    public Image[] getImages() {
        return images;
    }

    public void setImages(Image[] images) {
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalTracks() {
        return totalTracks;
    }

    public void setTotalTracks(long totalTracks) {
        this.totalTracks = totalTracks;
    }

    // Nested classes
    @Data
    public static class Image {
        private String url;
    }

    @Data
    public static class AlbumTracksContainer {
        private List<AlbumTrack> items;
    }

    @Data
    public static class AlbumTrack {
        private List<SpotifySongDto.Artist> artists;
        private String id;
        private String name;
    }
}
