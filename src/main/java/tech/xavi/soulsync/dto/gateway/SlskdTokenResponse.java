package tech.xavi.soulsync.dto.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties
@Data
public class SlskdTokenResponse{
    String token;
    String expires;
}
