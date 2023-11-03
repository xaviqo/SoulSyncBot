package tech.xavi.soulsync.service.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Log4j2
@Service
public class RunningTaskService {

    private final SoulSyncConfiguration.App appConfiguration;
    private final QueueService queueService;
    private final PlaylistService playlistService;
    private final SlskdGateway slskdGateway;
    private final DownloadService downloadService;
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    public RunningTaskService(
            ConfigurationService configurationService,
            QueueService queueService,
            PlaylistService playlistService,
            DownloadService downloadService,
            SlskdGateway slskdGateway,
            TaskScheduler taskScheduler
    ) {
        this.queueService = queueService;
        this.playlistService = playlistService;
        this.slskdGateway = slskdGateway;
        this.downloadService = downloadService;
        this.taskScheduler = taskScheduler;
        this.appConfiguration = configurationService.getConfiguration().app();
        this.configureTask();
    }

    private void configureTask(){
        int interval = appConfiguration.getIntervalMinutesScheduledTask();
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
        queueService.printQueueStatus();

        if (slskdGateway.isSlskdApiHealthy()) {
            downloadService.resetStuckSongs();
            playlistService.updateAllPlaylists();
            queueService.addUpdatedPlaylistsToQueue();
        } else {
            log.error("[runScheduledTask] - ATTENTION ---> No API response from Slskd");
        }
    }

}
