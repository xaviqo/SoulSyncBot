package tech.xavi.soulsync.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DownloadStatus {
    SUCCEDED("Succeeded"),
    CANCELLED("Cancelled"),
    ERRORED("Errored"),
    QUEDED("Queued");

    private final String progress;
}
