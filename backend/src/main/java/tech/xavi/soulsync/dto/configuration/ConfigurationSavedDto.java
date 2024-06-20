package tech.xavi.soulsync.dto.configuration;

import lombok.Builder;
import tech.xavi.soulsync.dto.shared.AlertData;

@Builder
public record ConfigurationSavedDto(AlertData alertData)  {
}
