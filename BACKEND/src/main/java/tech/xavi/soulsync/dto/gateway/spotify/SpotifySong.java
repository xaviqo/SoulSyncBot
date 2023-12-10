package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifySong {

    private Track track;

    public String getName() {
        return track.getName();
    }
    public String getId(){
        return track.getId();
    }

    public String getFirstArtist() {
        return track.getArtists().get(0).getName();
    }

    public String getAlbum(){
        return track.getAlbum().getName();
    }

    public List<String> getArtists(){
        return track.getArtists()
                .stream()
                .map(Artist::getName)
                .toList();
    }

    @Data
    public static class Track {
        private List<Artist> artists;
        private String name;
        private String id;
        private Album album;

        public void setAlbum(String name){
            this.album = new Album(name);
        }
    }

    @AllArgsConstructor
    @Data
    private static class Album {
        private String name;
    }

    @Data
    public static class Artist {
        private String name;
    }

}

