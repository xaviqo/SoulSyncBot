package tech.xavi.soulsync.dto.gateway.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties @Builder
public record LastReleaseDto(
        @JsonProperty("tag_name") String tagName,
        String message
) {
}
