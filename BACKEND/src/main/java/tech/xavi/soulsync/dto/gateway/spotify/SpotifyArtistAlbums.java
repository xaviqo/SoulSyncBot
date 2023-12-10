package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyArtistAlbums {

    private List<SpotifyAlbum> items;

}
