package tech.xavi.soulsync.dto.gateway.slskd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SlskdSearchResponse(
        List<SlskdFile> files,
        boolean hasFreeUploadSlot,
        String username
) {
}
