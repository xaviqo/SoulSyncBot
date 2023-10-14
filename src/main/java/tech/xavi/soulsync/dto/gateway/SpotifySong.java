package tech.xavi.soulsync.dto.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifySong {

    private Track track;

    public String getName() {
        return track.getName();
    }

    // We obtain better results
    // if we only select the first artist.
    public String getFirstArtist() {
        return track.getArtists().get(0).getName();
    }

    public List<String> getArtists(){
        return track.getArtists()
                .stream()
                .map(Artist::getName)
                .toList();
    }

    @Data
    private static class Track {
        private List<Artist> artists;
        private String name;
    }

    @Data
    private static class Artist {
        private String name;
    }
}

