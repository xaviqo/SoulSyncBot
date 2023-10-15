package tech.xavi.soulsync.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.model.Playlist;
import tech.xavi.soulsync.model.Song;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Log4j2
@Service
public class QueueService {

    private final BlockingQueue<Song> songsQueue;
    private final int MAX_RUNNERS;
    private final SearchService searchService;
    private final DownloadService downloadService;
    private final WatchlistService watchlistService;
    private final RateLimitDelayService delayService;

    public QueueService(
            @Value("${tech.xavi.soulsync.cfg.max-songs-downloading-at-a-time}") int maxRunners,
            SearchService searchService,
            DownloadService downloadService,
            WatchlistService watchlistService,
            RateLimitDelayService delayService
    ) {
        this.songsQueue = new ArrayBlockingQueue<>(1);
        this.MAX_RUNNERS = maxRunners;
        this.searchService = searchService;
        this.downloadService = downloadService;
        this.watchlistService = watchlistService;
        this.delayService = delayService;
    }

    @PostConstruct
    private void initRunners(){
        for (int i = 0; i < MAX_RUNNERS; i++) {
            new Thread(this::runner).start();
        }
    }

    private void addToQueue(Song song){
        log.debug("[addToQueue] - Waiting to add a new song to the queue: {}",song.getSearchInput());
        try {
            songsQueue.put(song);
            log.debug("[addToQueue] - Song added to queue: {}",song.getSearchInput());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlaylistToQueue(Playlist playlist){
        playlist.getSongs().forEach(this::addToQueue);
    }

    private void runner(){
        while (true){
            try {
                doProcess(songsQueue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    private void doProcess(Song song){
        double totalSec = 0;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("[doProcess] Process initiated for the song: {}. Total time elapsed: {} sec",
                song.getSearchInput(),
                stopWatch.getTotalTimeSeconds()
        );
        delayService.initSeek();
        stopWatch.stop();
        totalSec+=stopWatch.getTotalTimeSeconds();
        log.debug("[doProcess] Sent to search: {}. Time elapsed: {} sec, Total time elapsed {}",
                song.getSearchInput(),
                stopWatch.getTotalTimeSeconds(),
                totalSec
        );
        stopWatch.start();
        searchService.searchSong(song);
        stopWatch.stop();
        totalSec+=stopWatch.getTotalTimeSeconds();
        log.debug("[doProcess] Sent to download: {}. Time elapsed: {} sec, Total time elapsed {}",
                song.getSearchInput(),
                stopWatch.getTotalTimeSeconds(),
                totalSec
        );
        stopWatch.start();
        downloadService.prepareDownload(song);
        stopWatch.stop();
        totalSec+=stopWatch.getTotalTimeSeconds();
        log.debug("[doProcess] Sent to update in DB: {}. Time elapsed: {} sec, Total time elapsed {}",
                song.getSearchInput(),
                stopWatch.getTotalTimeSeconds(),
                totalSec
        );
        stopWatch.start();
        watchlistService.updateSongStatus(song);
        delayService.finishSeek();
        stopWatch.stop();
        totalSec+=stopWatch.getTotalTimeSeconds();
        log.debug("[doProcess] Process finished for song: {}. Time elapsed: {} sec, Total time elapsed {}",
                song.getSearchInput(),
                stopWatch.getTotalTimeSeconds(),
                totalSec
        );
    }


    public void printQueueStatus(){
        log.debug("[QUEUE_STATUS] ----> Total songs waiting: {}",songsQueue.size());
        log.debug("[QUEUE_STATUS] ----> Remaining Capacity: {}",songsQueue.remainingCapacity());

    }
}
