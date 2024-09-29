package tech.xavi.soulsync.dto.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class WatchdogTaskWrapper {

    private final UUID taskId;
    private final String taskGroup;
    @Setter private String taskName;
    @Setter private long lastUpdate;

    public WatchdogTaskWrapper(String taskGroup, String taskName, long lastUpdate) {
        this.taskId = UUID.randomUUID();
        this.taskGroup = taskGroup;
        this.taskName = taskName;
        this.lastUpdate = lastUpdate;
    }

}
