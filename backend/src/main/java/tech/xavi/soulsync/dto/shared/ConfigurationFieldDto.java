package tech.xavi.soulsync.dto.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfigurationFieldDto(
        String name,
        Object value
) {
}
