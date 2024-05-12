package tech.xavi.soulsync.dto.gateway.spotify;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SpotifyArtistDto {
    private String id;
    private String name;
}
