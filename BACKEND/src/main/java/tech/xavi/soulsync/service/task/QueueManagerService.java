package tech.xavi.soulsync.service.task;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.entity.sub.SoulSyncConfiguration;
import tech.xavi.soulsync.service.config.ConfigurationService;
import tech.xavi.soulsync.service.main.SongService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class QueueManagerService {

    private final BlockingQueue<Song> songBlockingQueue;
    private final Set<String> pausedPlaylists;
    private final Set<String> songIdsQueue;
    private final SearchService searchService;
    private final DownloadService downloadService;
    private final PauseService delayService;
    private final SongService songService;
    private final Set<Thread> activeProcesses;

    public QueueManagerService(
            SearchService searchService,
            DownloadService downloadService,
            PauseService delayService,
            SongService songService
    ) {
        this.songBlockingQueue = new ArrayBlockingQueue<>(1);
        this.songIdsQueue = Collections.synchronizedSet(new HashSet<>());
        this.pausedPlaylists = Collections.synchronizedSet(new HashSet<>());
        this.searchService = searchService;
        this.downloadService = downloadService;
        this.delayService = delayService;
        this.songService = songService;
        this.activeProcesses = new HashSet<>();
    }

    @PostConstruct
    private void initProcesses(){
        int simultaneousProcesses = getConfiguration().getTotalSimultaneousProcesses();
        for (int i = 0; i < simultaneousProcesses; i++) {
            Thread process = new Thread(this::runner);
            activeProcesses.add(process);
            process.start();
        }
        log.debug("[initProcesses] A total of '{}' simultaneous processes are ready:",simultaneousProcesses);
    }

    private void doProcess(Song song) {
        song.setSearchId(UUID.randomUUID());
        song.addAttempt();

        double totalSec = 0;
        songIdsQueue.add(song.getSpotifyId());
        String searchInput = song.getSearchInput();
        log.debug("[doProcess] Started seek process for search: '{}'",searchInput);

        totalSec += performService("Init Seek", delayService::initSeek, searchInput);
        totalSec += performService("Search", () -> searchService.searchSong(song), searchInput);
        totalSec += performService("Download", () -> downloadService.prepareDownload(song), searchInput);
        totalSec += performService("Update Song Status", () -> songService.updateSongStatus(song), searchInput);
        totalSec += performService("Finish Seek", delayService::finishSeek, searchInput);

        log.debug("[doProcess] Finished seek process for search: '{}' - Total time elapsed {} sec",searchInput,totalSec);
        songIdsQueue.remove(song.getSpotifyId());
    }

    public void rebootProcesses(){
        activeProcesses.forEach(Thread::interrupt);
        this.initProcesses();
    }

    public void updateQueue(){
        log.debug("[runScheduledTask] - Scheduled task executed");
        songService
                .findByStatus(SongStatus.WAITING)
                .ifPresent( songSet -> {
                    songSet.stream()
                            .filter(this::songReachedThreshold)
                            .forEach( song -> {
                                boolean isInQueue = songIdsQueue.contains(song.getSpotifyId());
                                if (!isInQueue) addSongToQueue(song);
                            });
                });
    }

    public Set<String> getSongsInQueueIds(){
        return songIdsQueue;
    }

    private void addSongToQueue(Song song){
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            log.debug("[addToQueue] - Waiting queue a new song... '{}' - Attempt: {}", song.getSearchInput(),song.getAttempts());
            songBlockingQueue.put(song);
            sw.stop();
            long elapsed = TimeUnit.MILLISECONDS.toMinutes(sw.getTotalTimeMillis());
            log.debug("[addToQueue] - Song added to queue: '{}' - Elapsed time: {}", song.getSearchInput(),elapsed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean songReachedThreshold(Song song){
        long songThreshold = getAdjustedCheckTimeThreshold(song);
        boolean shouldKeep = songThreshold <= System.currentTimeMillis();
        log.debug("[songReachedThreshold] - Song: {} | Last Check: {} | Should Keep: {}", song.getName(), song.getLastCheck(), shouldKeep);
        return shouldKeep;
    }

    private long getAdjustedCheckTimeThreshold(Song song){
        int minimumMinutes = getConfiguration().getMinimumMinutesBtwSongCheck();
        return  (song.getLastCheck() != 0 || minimumMinutes > 0)
                ? song.getLastCheck() + TimeUnit.MINUTES.toMillis(minimumMinutes)
                : 0;
    }

    private void runner(){
        while (true){
            try {
                Song song = songBlockingQueue.take();
                if (shouldDownloadSongByPauseFilter(song)) {
                    doProcess(song);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private double performService(String serviceName, Runnable serviceFunction, String searchInput) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("[performService] - [{}] - STARTED - Search input: '{}'",
                serviceName,
                searchInput
        );
        serviceFunction.run();
        stopWatch.stop();
        double timeSeconds = stopWatch.getTotalTimeSeconds();
        log.debug("[performService] - [{}] - FINISHED - Search input: '{}' - Time elapsed: {} sec",
                serviceName,
                searchInput,
                timeSeconds
        );
        return timeSeconds;
    }

    private boolean shouldDownloadSongByPauseFilter(Song song) {
        Set<String> plIdsWhereSongIsPresent = songService
                .getPlaylistIdsFromSongId(song.getSpotifyId());
        long totalPaused = plIdsWhereSongIsPresent.stream()
                .filter(pausedPlaylists::contains)
                .count();
        return plIdsWhereSongIsPresent.size() != totalPaused;
    }

    public void pausePlaylist(String playlistId){
        pausedPlaylists.add(playlistId);
    }

    public void unpausePlaylist(String playlistId){
        pausedPlaylists.remove(playlistId);
    }

    public boolean isPlaylistPaused(String plId){
        return pausedPlaylists.contains(plId);
    }

    public void printQueueStatus(){
        log.debug("[QUEUE_STATUS] ----> Total songs in queue: {}", songIdsQueue.size());
        songIdsQueue.forEach( id -> log.debug("[QUEUE_STATUS] ----> {}", id) );
    }

    private SoulSyncConfiguration.App getConfiguration(){
        return ConfigurationService.instance().cfg().app();
    }
}
