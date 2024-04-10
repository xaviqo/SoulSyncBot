package tech.xavi.soulsync.service.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.service.task.task.Task;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Log4j2
@Service
public class TaskManagerService {

    private final Set<Task> tasks;
    private final AtomicLong totalElapsed = new AtomicLong();

    public TaskManagerService(Set<Task> tasks) {
        this.tasks = tasks.stream()
                .sorted(Comparator.comparingInt(Task::getOrder))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Scheduled(fixedDelay = 60000 * 2)
    public void runAllTasks() {
        log.debug("Running all tasks");

        tasks.forEach(task -> {
            log.debug("Running task  ({}/{}) :: {}",
                    task.getOrder(),
                    tasks.size(),
                    task.getTaskName()
            );
            task.execute();
            long taskElapsed = task.getElapsedSeconds();
            log.debug("Finished task :: {} :: Time elapsed --> {}",
                    task.getTaskName(),
                    taskElapsed
            );
            totalElapsed.addAndGet(taskElapsed);
        });

        log.debug("Finished all tasks :: Total elapsed --> {}",totalElapsed);
        totalElapsed.addAndGet(0);
    }

}
