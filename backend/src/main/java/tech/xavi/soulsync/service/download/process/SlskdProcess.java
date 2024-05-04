package tech.xavi.soulsync.service.download.process;

import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.task.Process;

import java.util.concurrent.CompletableFuture;

public abstract class SlskdProcess implements Process {

    public CompletableFuture<Void> execute(SlskdRequest request) {
        return execute();
    }

    @Override
    public CompletableFuture<Void> execute() {
        return getCompletableFuture();
    }

    @Override
    public String getTaskType() {
        return "SLSKD";
    }
}
