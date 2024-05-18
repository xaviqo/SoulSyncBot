package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
public class SpotifyAlbumDto {
    String id;
    String name;
    @JsonProperty("album_type")
    String albumType;
    List<SpotifyArtistDto> artists;
    SpotifyAlbumTrackContainerDto tracks;
    @JsonProperty("total_tracks")
    int totalTracks;
    SpotifyImageDto[] images;
    public String getCover() {
        return this.images != null && this.images.length > 0
                ? this.images[0].getUrl()
                : null;
    }

}
