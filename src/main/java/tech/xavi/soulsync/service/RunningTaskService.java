package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.model.Playlist;
import tech.xavi.soulsync.model.Song;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class RunningTaskService {

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
        this.taskExecutor = Executors.newScheduledThreadPool(1);
        this.configureTask(intervalMinutes);
    }

    private void configureTask(int interval){
        log.debug("[configureTask] - Interval between tasks has been updated: {} minutes",interval);
        taskExecutor.scheduleWithFixedDelay(
                this::runScheduledTask,
                interval,
                interval,
                TimeUnit.MINUTES
        );
    }

    protected void runScheduledTask(){
        queueService.printQueueStatus();
        if (slskdGateway.isSlskdApiHealthy()) {
            List<Playlist> pendingPlaylists = watchlistService.getWaitingPlaylists();
            log.debug("[runScheduledTask] - Scheduled task is executed. " +
                    "Total pending playlists: {}",pendingPlaylists.size());
            pendingPlaylists.forEach( pl -> {
                if (!queueService.isPlaylistInQueue(pl)) {
                    for (Song song : pl.getSongs())
                        song.setSearchId(UUID.randomUUID());
                    queueService.seek(pl);
                } else {
                    log.debug("[runScheduledTask] - Playlist with id ({}) is already in the work queue and will not be added",pl.getSpotifyId());
                }
            });

        } else {
            log.error("[runScheduledTask] - ATTENTION ---> No API response from Slskd");
        }
    }


}
