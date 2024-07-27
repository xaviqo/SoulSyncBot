package tech.xavi.soulsync.dto.gateway.slskd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record SlskdSearchResponse(
        List<SlskdFile> files,
        boolean hasFreeUploadSlot,
        String username
) {
}
