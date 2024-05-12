package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tech.xavi.soulsync.entity.db.Artist;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifySongDto {

    @Getter
    private Track track;
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

    public Set<Artist> getArtists(){
        return track.getArtists();
    }

    public Set<String> getArtistsAsString(){
        return track.getArtists()
                .stream()
                .map(Artist::getName)
                .collect(Collectors.toSet());
    }


    @Data
    public static class Track {
        private Set<Artist> artists;
        private String name;
        private String id;
        private Album album;
        @JsonProperty("album")
        public void setAlbum(Album album){
            this.album = album;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Album {
        private String name;
    }



}