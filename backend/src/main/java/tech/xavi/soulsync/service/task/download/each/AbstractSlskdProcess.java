package tech.xavi.soulsync.service.task.download.each;

import tech.xavi.soulsync.service.task.Process;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractSlskdProcess implements Process {

    public String getTaskType() {
        return "SLSKD";
    }

    public CompletableFuture<Long> execute() {
        return null;
    }

    public String getTaskName() {
        return null;
    }

    public int getOrder() {
        return 0;
    }
}
