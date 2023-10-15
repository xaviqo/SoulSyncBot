package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.model.Playlist;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class RunningTaskService {

    private int configuredInterval;
    private final ScheduledExecutorService taskExecutor;
    private final WatchlistService watchlistService;
    private final QueueService queueService;
    private final SlskdGateway slskdGateway;

    public RunningTaskService(
            @Value("${tech.xavi.soulsync.cfg.interval-minutes-scheduled-task}") int intervalMinutes,
            WatchlistService watchlistService,
            QueueService queueService,
            SlskdGateway slskdGateway
    ) {
        this.watchlistService = watchlistService;
        this.queueService = queueService;
        this.slskdGateway = slskdGateway;
        this.configuredInterval = intervalMinutes;
        this.taskExecutor = Executors.newScheduledThreadPool(1);
        this.configureTask(configuredInterval);
    }

    private void configureTask(int interval){
        log.debug("[configureTask] - Interval between tasks has been updated: {} minutes",interval);
        taskExecutor.scheduleWithFixedDelay(
                this::runScheduledTask,
                0,
                interval,
                TimeUnit.MINUTES
        );
    }

    private void runScheduledTask(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        queueService.printQueueStatus();
        if (slskdGateway.healthCheck()) {
            List<Playlist> pendingPlaylists = watchlistService.getWaitingPlaylists();
            log.debug("[runScheduledTask] - Scheduled task is executed. " +
                    "Total pending playlists: {}",pendingPlaylists.size());
            pendingPlaylists.forEach( pl -> {
                if (!queueService.isPlaylistInQueue(pl)) {
                    log.debug("[runScheduledTask] - Playlist with id ({}) is not in the work queue and will be added",pl.getSpotifyId());
                    pl.getSongs().forEach(song -> song.setSearchId(UUID.randomUUID()));
                    CompletableFuture.runAsync(() -> {
                        queueService.addPlaylistToQueue(pl);
                        watchlistService.updateWatchlist(pl);
                        queueService.removePlaylistFromQueue(pl);
                    });
                } else {
                    log.debug("[runScheduledTask] - Playlist with id ({}) is already in the work queue and will not be added",pl.getSpotifyId());
                }
            });

        } else {
            log.error("[runScheduledTask] - ATTENTION ---> No API response from Slskd");
        }
        stopWatch.stop();
        log.debug("[runScheduledTask] - " +
                "The scheduled task has been completed in {} seconds.",stopWatch.getTotalTimeSeconds());
    }


}
