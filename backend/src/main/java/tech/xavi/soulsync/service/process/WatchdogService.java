package tech.xavi.soulsync.service.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.configuration.WatchdogTaskWrapper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@Service
public class WatchdogService {

    private static final boolean useWatchdog = false;
    private static final long MAX_TASK_DURATION_MS = 600000; // 10MIN
    private static final long THREADS_COMPLETION_WAIT_MS = 120000; // 2MIN

    private final Map<UUID, WatchdogTaskWrapper> taskLastActivityMap = new ConcurrentHashMap<>();
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final AtomicBoolean isThreadCreationAllowed;

    public WatchdogService(
            ThreadPoolTaskExecutor taskExecutor,
            ThreadPoolTaskScheduler taskScheduler
    ) {
        this.taskExecutor = taskExecutor;
        this.taskScheduler = taskScheduler;
        this.isThreadCreationAllowed = new AtomicBoolean(true);
    }

    @Scheduled(fixedRate = 60000)
    public void monitorTasks() {
        if (!useWatchdog) return;
        final long currentTime = System.currentTimeMillis();

        taskLastActivityMap.forEach( (taskId, wrapper) -> {
            if (currentTime - wrapper.getLastUpdate() > MAX_TASK_DURATION_MS) {
                log.warn("Task {} exceeded max duration. Attempting to handle.", taskId);
                if (isThreadCreationAllowed()) {
                    try {
                        handleStuckTask(taskId);
                    } catch (InterruptedException e) {
                        log.warn("Error handling stuck tasks: ", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public UUID registerTask(String taskGroup) {
        if (!useWatchdog) return UUID.randomUUID();
        WatchdogTaskWrapper wtw = new WatchdogTaskWrapper(taskGroup,null, 0);
        taskLastActivityMap.put(wtw.getTaskId(), wtw);
        return wtw.getTaskId();
    }

    public void updateTaskActivity(UUID taskId, String task) {
        if (!useWatchdog) return;
        final WatchdogTaskWrapper wtw = taskLastActivityMap.get(taskId);
        wtw.setLastUpdate(System.currentTimeMillis());
        wtw.setTaskName(task);
        taskLastActivityMap.put(taskId, wtw);
    }

    public void completeTask(UUID taskId) {
        if (!useWatchdog) return;
        taskLastActivityMap.remove(taskId);
    }

    private void handleStuckTask(UUID taskId) throws InterruptedException {
        if (!useWatchdog) return;
        WatchdogTaskWrapper taskWrapper = taskLastActivityMap.get(taskId);

        isThreadCreationAllowed.set(false);
        log.debug("Task {} is stuck. Reloading SoulSync threads. Waiting for {}ms", taskWrapper, THREADS_COMPLETION_WAIT_MS);
        Thread.sleep(THREADS_COMPLETION_WAIT_MS);

        try {
            log.debug("Handling stuck task {}. Shutting down task executor and scheduler.", taskWrapper);

            taskExecutor.shutdown();
            boolean executorTerminated = taskExecutor.getThreadPoolExecutor().awaitTermination(THREADS_COMPLETION_WAIT_MS, TimeUnit.MILLISECONDS);
            if (executorTerminated) {
                log.info("Task executor shut down gracefully.");
            } else {
                taskExecutor.getThreadPoolExecutor().shutdownNow();
                log.warn("Task executor did not terminate in the specified time. Forced shutdown initiated.");
            }
            taskExecutor.initialize();
            log.info("Task executor has been re-initialized.");

            taskScheduler.shutdown();
            boolean schedulerTerminated = taskScheduler.getScheduledExecutor().awaitTermination(THREADS_COMPLETION_WAIT_MS, TimeUnit.MILLISECONDS);
            if (schedulerTerminated) {
                log.info("Task scheduler shut down gracefully.");
            } else {
                taskScheduler.getScheduledExecutor().shutdownNow();
                log.warn("Task scheduler did not terminate in the specified time. Forced shutdown initiated.");
            }
            taskScheduler.initialize();
            log.info("Task scheduler has been re-initialized.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted while handling stuck task {}.", taskWrapper, e);
            throw e;
        } finally {
            isThreadCreationAllowed.set(true);
            log.debug("Creation of new threads resumed after handling task {}.", taskWrapper);
        }

        taskLastActivityMap.clear();
    }

    public boolean isThreadCreationAllowed(){
        return isThreadCreationAllowed.get();
    }

}
