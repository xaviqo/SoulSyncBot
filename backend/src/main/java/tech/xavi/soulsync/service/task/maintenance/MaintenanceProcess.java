package tech.xavi.soulsync.service.task.maintenance;

import lombok.Getter;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.service.task.Process;

import java.util.concurrent.CompletableFuture;

@Getter
public abstract class MaintenanceProcess implements Process {

    private final StopWatch stopWatch;

    public MaintenanceProcess() {
        this.stopWatch = new StopWatch();
    }

    @Override
    public String getTaskType() {
        return "MAINTENANCE";
    }

    @Override
    public CompletableFuture<Void> execute() {
        return null;
    }

    @Override
    public String getTaskName() {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
