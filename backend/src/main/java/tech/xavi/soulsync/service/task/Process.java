package tech.xavi.soulsync.service.task;

import org.springframework.util.StopWatch;

import java.util.concurrent.CompletableFuture;

public interface Process {

    StopWatch stopWatch = new StopWatch();

    CompletableFuture<Long> execute();

    String getTaskName();

    String getTaskType();

    int getOrder();

    default void startTimer(){
        stopWatch.start();
    }

    default CompletableFuture<Long> getElapsedSeconds(){
        stopWatch.stop();
        long elapsed = stopWatch.getTotalTimeMillis() / 1000;
        return CompletableFuture.completedFuture(elapsed);
    }
}
