package tech.xavi.soulsync.service.task.task;

import org.springframework.util.StopWatch;
import tech.xavi.soulsync.configuration.globals.TaskName;

import java.util.concurrent.CompletableFuture;

public interface Task {

    StopWatch stopWatch = new StopWatch();

    CompletableFuture<Long> execute();

    TaskName getTaskName();

    int getOrder();

    default void startTimer(){
        stopWatch.start();
    }

    default long getElapsedSeconds(){
        stopWatch.stop();
        return stopWatch.getTotalTimeMillis() / 1000;
    }
}
