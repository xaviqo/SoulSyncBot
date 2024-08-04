package tech.xavi.soulsync.dto.configuration;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tech.xavi.soulsync.dto.shared.AlertData;

@Builder @Setter @Getter
public class AppVersionDto {
    String current;
    String latest;
    AlertData alertData;
}
