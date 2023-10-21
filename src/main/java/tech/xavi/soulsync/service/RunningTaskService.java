package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.gateway.SlskdGateway;

@Log4j2
@Service
public class RunningTaskService {

    private final QueueService queueService;
    private final PlaylistService playlistService;
    private final SlskdGateway slskdGateway;
    private final DownloadService downloadService;

    public RunningTaskService(
            @Value("${tech.xavi.soulsync.cfg.interval-minutes-scheduled-task}") int intervalMinutes,
            QueueService queueService,
            PlaylistService playlistService,
            DownloadService downloadService,
            SlskdGateway slskdGateway
    ) {
        this.queueService = queueService;
        this.playlistService = playlistService;
        this.slskdGateway = slskdGateway;
        this.downloadService = downloadService;
        this.configureTask(intervalMinutes);
    }

    private void configureTask(int interval){
        log.debug("[configureTask] - Interval between tasks has been updated: {} minutes",interval);
    }

    @Scheduled(fixedRate = 10000)
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
