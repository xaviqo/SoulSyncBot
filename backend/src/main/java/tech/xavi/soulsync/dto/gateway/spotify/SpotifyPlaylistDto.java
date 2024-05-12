package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistDto {

    private String id;
    private SpotifyTrackContainerDto tracks;
    private List<SpotifyArtistDto> artists;
    private SpotifyImageDto[] images;
    private String name;
    private long totalTracks;

    @JsonProperty("tracks")
    private void unpackNestedTracks(JsonNode tracksNode) {
        this.totalTracks = tracksNode.get("total").asLong();
    }
    public String getCover() {
        return this.images != null && this.images.length > 0
                ? this.images[0].getUrl()
                : null;
    }
}
