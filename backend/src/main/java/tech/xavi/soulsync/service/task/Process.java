package tech.xavi.soulsync.service.task;

import java.util.concurrent.CompletableFuture;

public interface Process {

    CompletableFuture<Void> execute();

    String getTaskName();

    String getTaskType();

    int getOrder();

    default CompletableFuture<Void> getCompletableFuture(){
        return CompletableFuture.completedFuture(null);
    }

}
