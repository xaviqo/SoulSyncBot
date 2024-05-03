package tech.xavi.soulsync.service.task;

import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@SuperBuilder
@Log4j2
public abstract class AbstractProcessHandler implements Process {

    private Process currentProcess;
    private final Set<Process> processes;
    private final AtomicLong totalElapsed;
    private final String TASK_TYPE;

    public AbstractProcessHandler(Set<? extends Process> tasks) {
        this.totalElapsed = new AtomicLong();
        this.TASK_TYPE = this.getTaskType(tasks);
        this.processes = tasks.stream()
                .sorted(Comparator.comparingInt(Process::getOrder))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public CompletableFuture<Long> execute() {
        startTimer();
        processes.forEach(task -> {
            currentProcess = task;
            try {
                log.debug("Running {} task  ({}/{}) :: {}",
                        TASK_TYPE,
                        currentProcess.getOrder(),
                        processes.size(),
                        currentProcess.getTaskName()
                );
                long taskElapsed = currentProcess.execute().get();
                log.debug("Finished {} task :: {} :: Time elapsed --> {}",
                        TASK_TYPE,
                        currentProcess.getTaskName(),
                        taskElapsed
                );
                totalElapsed.addAndGet(taskElapsed);
            } catch (Exception e) {
                log.debug("Error '{}' @ {} task  ({}/{}) :: {}",
                        e.getMessage(),
                        TASK_TYPE,
                        currentProcess.getOrder(),
                        processes.size(),
                        currentProcess.getTaskName()
                );
                e.printStackTrace();
            }
        });
        log.debug("Finished all {} tasks :: Total elapsed --> {}",
                TASK_TYPE,
                totalElapsed
        );
        totalElapsed.addAndGet(0);
        return getElapsedSeconds();
    }

    @Override
    public String getTaskName() {
        return "";
    }

    @Override
    public String getTaskType() {
        return "";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private String getTaskType(Set<? extends Process>  tasks){
        for (Process process : tasks) { return process.getTaskType(); }
        return null;
    }

}
