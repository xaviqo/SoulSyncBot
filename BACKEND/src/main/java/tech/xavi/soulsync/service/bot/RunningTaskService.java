package tech.xavi.soulsync.service.bot;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.sub.SoulSyncConfiguration;
import tech.xavi.soulsync.service.configuration.ConfigurationService;
import tech.xavi.soulsync.service.relocate.RelocateService;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Log4j2
@Service
public class RunningTaskService {

    private final QueueService queueService;
    private final PlaylistService playlistService;
    private final DownloadService downloadService;
    private final RelocateService relocateService;
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    public RunningTaskService(
            QueueService queueService,
            PlaylistService playlistService,
            DownloadService downloadService,
            RelocateService relocateService,
            TaskScheduler taskScheduler
    ) {
        this.queueService = queueService;
        this.playlistService = playlistService;
        this.downloadService = downloadService;
        this.relocateService = relocateService;
        this.taskScheduler = taskScheduler;
        this.configureTask();
    }

    private void configureTask(){
        int interval = getConfiguration().getIntervalMinutesScheduledTask();
        if (scheduledTask != null) {
            log.debug("[configureTask] - The running task has been stopped by the user, " +
                    "a new time interval of {} minutes is set",interval);
            scheduledTask.cancel(true);
        }
        scheduledTask = taskScheduler.scheduleWithFixedDelay(
                this::runScheduledTask,
                Duration.ofMinutes(interval)
        );

        log.debug("[configureTask] - Interval between tasks has been updated: {} minutes",interval);
    }

    protected void runScheduledTask(){
        log.debug("[runScheduledTask] - Task started");
        queueService.printQueueStatus();
        downloadService.updateSongsStatus();
        playlistService.updatePlaylistsFromSpotify();
        queueService.updateQueue();
        relocateService.moveFinishedPlaylistsSongs();
        log.debug("[runScheduledTask] - Task finished");
    }

    private SoulSyncConfiguration.App getConfiguration(){
        return ConfigurationService.instance().cfg().app();
    }

}
