package tech.xavi.soulsync.service.task.maintenance;

import tech.xavi.soulsync.service.task.Process;

import java.util.concurrent.CompletableFuture;

public abstract class MaintenanceProcess implements Process {

    // DELETE COMPLETE, DELETE OLD SEARCHES

    @Override
    public String getTaskType() {
        return "MAINTENANCE";
    }

    @Override
    public CompletableFuture<Long> execute() {
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
