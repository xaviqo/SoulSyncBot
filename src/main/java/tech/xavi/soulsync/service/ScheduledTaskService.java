package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.model.Playlist;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class ScheduledTaskService {

    private int configuredInterval;
    private final ScheduledExecutorService taskExecutor;
    private final PlaylistService playlistService;
    private final WatchlistService watchlistService;

    public ScheduledTaskService(
            @Value("${tech.xavi.soulsync.cfg.interval-minutes-scheduled-task}") int intervalMinutes,
            @Value("${tech.xavi.soulsync.cfg.max-concurrent-threads}") int maxConcurrentTasks,
            PlaylistService playlistService,
            WatchlistService watchlistService
    ) {
        this.playlistService = playlistService;
        this.watchlistService = watchlistService;
        this.configuredInterval = intervalMinutes;
        this.taskExecutor = Executors.newScheduledThreadPool(maxConcurrentTasks);
        this.configureTask(configuredInterval);
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

    private void runScheduledTask(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List< Playlist > pendingPlaylists = watchlistService.getWaitingPlaylists();
        log.debug("[runScheduledTask] - Scheduled task is executed. " +
                "Total pending playlists: {}",pendingPlaylists.size());
        pendingPlaylists.forEach( pl -> {
            CompletableFuture.runAsync(() -> {
                playlistService.checkPlaylist(pl);
                watchlistService.updateWatchlist(pl);
            });
        });
        stopWatch.stop();
        log.debug("[runScheduledTask] - " +
                "The scheduled task has been completed in {} seconds.",stopWatch.getTotalTimeSeconds());
    }


}
