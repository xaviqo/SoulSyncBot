package tech.xavi.soulsync.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DownloadStatus {
    SUCCEDED("Succeeded"),
    CANCELLED("Cancelled"),
    ERRORED("Errored"),
    QUEDED("Queued");

    private final String progress;
}
