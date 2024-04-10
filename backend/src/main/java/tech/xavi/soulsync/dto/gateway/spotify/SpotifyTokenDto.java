package tech.xavi.soulsync.dto.gateway.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public record SpotifyTokenDto(@JsonProperty("access_token") String token, @JsonProperty("expires_in") long expires) {
}