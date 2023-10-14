package tech.xavi.soulsync.dto.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties
@Data
public class SpotifyTokenRes {

    @JsonProperty("access_token")
    String token;

    @JsonProperty("expires_in")
    long expires;

}
