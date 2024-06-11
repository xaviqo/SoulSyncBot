package tech.xavi.soulsync.service.process;

import java.util.concurrent.CompletableFuture;

public interface Process {

    CompletableFuture<Boolean> execute();

    String getTaskName();

    String getTaskType();

    int getOrder();

}
