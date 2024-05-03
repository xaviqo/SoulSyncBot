package tech.xavi.soulsync.service.task.download.each;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class SearchProcess implements Pro {

    @Override
    public String getTaskType() {
        return "search";
    }

    @Override
    public CompletableFuture<Long> execute() {
        startTimer();
        try {
            System.out.println("a mimir");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return getElapsedSeconds();
    }

    @Override
    public String getTaskName() {
        return super.getTaskName();
    }

    @Override
    public int getOrder() {
        return super.getOrder();
    }
}
