package tech.xavi.soulsync.dto.gateway.slskd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SlskdDirectory(
        String directory,
        int fileCount,
        List<SlskdFile> files
) {}