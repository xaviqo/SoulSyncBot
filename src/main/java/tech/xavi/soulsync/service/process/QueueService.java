package tech.xavi.soulsync.service.process;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.entity.Playlist;
import tech.xavi.soulsync.entity.PlaylistStatus;
import tech.xavi.soulsync.entity.Song;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Log4j2
@Service
public class QueueService {

    private final BlockingQueue<Song> songsQueue;
    private final Map<String,Integer> playlistRemainingSongs;
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
        this.playlistRemainingSongs = Collections.synchronizedMap(new HashMap<>());
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

    public void addUpdatedPlaylistsToQueue(){
        List<Playlist> pendingPlaylists = watchlistService.getPlaylists();
        log.debug("[runScheduledTask] - Scheduled task is executed. " +
                "Total pending playlists: {}",pendingPlaylists.size());
        pendingPlaylists.forEach( pl -> {
            if (!isPlaylistQueued(pl.getSpotifyId())) {
                log.debug("[runScheduledTask] - Playlist with id ({}) is NOT in the work queue " +
                        "and will be added",pl.getSpotifyId());
                for (Song song : pl.getSongs())
                    song.setSearchId(UUID.randomUUID());
                seek(pl);
            } else {
                log.debug("[runScheduledTask] - Playlist with id ({}) is already in the work queue " +
                        "and WON'T be added", pl.getSpotifyId());
            }
        });
    }

    @Async("asyncExecutor")
    public void seek(Playlist playlist){
        addPlaylistToQueue(playlist);
        watchlistService.updateWatchlist(playlist);
    }

    public synchronized void addPlaylistToQueue(Playlist playlist){
        String playlistId = playlist.getSpotifyId();
        playlistRemainingSongs.put(
                playlistId,
                playlist.getLastTotalTracks()
        );
        playlist.getSongs().forEach(this::addSongToQueue);
        log.debug("[addPlaylistToQueue] - Playlist added to queue: {}",playlistId);
    }

    public boolean isPlaylistQueued(String playlistId){
        return playlistRemainingSongs.containsKey(playlistId);
    }

    private void addSongToQueue(Song song){
        log.debug("[addToQueue] - Waiting to add a new song to the queue: {}",song.getSearchInput());
        try {
            songsQueue.put(song);
            log.debug("[addToQueue] - Song added to queue: {}",song.getSearchInput());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void decreaseRemainingPlaylistTotal(Playlist playlist){
        String playlistId = playlist.getSpotifyId();
        int updatedRemaining = playlistRemainingSongs.get(playlistId)-1;
        if (updatedRemaining < 1) {
            log.debug("[addToQueue] - The playlist '{}' with id '{}' has finished the process " +
                    "for all his queued songs.",playlist.getName(),playlistId);
            playlistRemainingSongs.remove(playlistId);
        } else {
            playlistRemainingSongs.put(playlistId,updatedRemaining);
        }
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

    private void doProcess(Song song) {
        double totalSec = 0;
        String searchInput = song.getSearchInput();
        log.debug("[doProcess] Started seek process for search: '{}'",searchInput);

        totalSec += performService("Init Seek", delayService::initSeek, searchInput);
        totalSec += performService("Search", () -> searchService.searchSong(song), searchInput);
        totalSec += performService("Download", () -> downloadService.prepareDownload(song), searchInput);
        totalSec += performService("Update Song Status", () -> watchlistService.updateSongStatus(song), searchInput);
        totalSec += performService("Finish Seek", delayService::finishSeek, searchInput);

        log.debug("[doProcess] Finished seek process for search: '{}' - Total time elapsed {} sec",searchInput,totalSec);

        decreaseRemainingPlaylistTotal(song.getPlaylist());
    }

    private double performService(String serviceName, Runnable serviceFunction, String searchInput) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("[performService] Started Process [{}] for search '{}'",serviceName,searchInput);
        serviceFunction.run();
        stopWatch.stop();
        double timeSeconds = stopWatch.getTotalTimeSeconds();
        log.debug("[performService] Finished Process [{}] for search '{}¡, Time elapsed: {} sec",
                serviceName,
                searchInput,
                timeSeconds
        );
        return timeSeconds;
    }

    public void printQueueStatus(){
        log.debug("[QUEUE_STATUS] ----> Total playlists queue: {}", playlistRemainingSongs.size());
        log.debug("[QUEUE_STATUS] ----> Playlists in queue: {}", Arrays.toString(playlistRemainingSongs.values().toArray()));
    }

    public PlaylistStatus getPlaylistQueueStatus(String spotifyId){
        return isPlaylistQueued(spotifyId)
                ? PlaylistStatus.QUEUED
                : PlaylistStatus.WAITING;
    }
}
