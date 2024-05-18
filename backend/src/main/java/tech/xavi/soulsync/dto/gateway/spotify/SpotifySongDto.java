package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifySongDto {

    @Getter
    public SpotifyTrackDto track;
    public String getName() {
        return track.getName();
    }
    public String getId(){
        return track.getId();
    }
    public String getAlbum(){
        return track.getAlbum().getName();
    }

    public void setId(String id){
        track.setId(id);
    }

    public List<SpotifyArtistDto> getArtists(){
        return track.getArtists();
    }

}