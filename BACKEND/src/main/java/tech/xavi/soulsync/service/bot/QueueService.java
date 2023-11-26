package tech.xavi.soulsync.service.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.entity.*;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
public class QueueService {

    private final BlockingQueue<Song> songsQueue;
    private final Map<String,Integer> playlistRemainingSongs;
    private final SearchService searchService;
    private final DownloadService downloadService;
    private final WatchlistService watchlistService;
    private final PauseService delayService;
    private final ThreadPoolTaskExecutor executor;
    private final PlaylistRepository playlistRepository;
    private Set<Thread> activeProcesses;

    public QueueService(
            SearchService searchService,
            DownloadService downloadService,
            WatchlistService watchlistService,
            PauseService delayService,
            PlaylistRepository playlistRepository
    ) {
        this.songsQueue = new ArrayBlockingQueue<>(1);
        this.playlistRemainingSongs = Collections.synchronizedMap(new HashMap<>());
        this.searchService = searchService;
        this.downloadService = downloadService;
        this.watchlistService = watchlistService;
        this.delayService = delayService;
        this.playlistRepository = playlistRepository;
        this.activeProcesses = new HashSet<>();
        int poolSize = ConfigurationService.instance().cfg().app().getMaxSongsDownloadingSameTime();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(100);
        executor.initialize();
        this.executor = executor;
    }

    @PostConstruct
    private void initProcesses(){
        int simultaneousProcesses = getConfiguration().getTotalSimultaneousProcesses();
        for (int i = 0; i < simultaneousProcesses; i++) {
            Thread process = new Thread(this::runner);
            activeProcesses.add(process);
            process.start();
        }
    }

    public void stopProcesses(){
        activeProcesses.forEach(Thread::interrupt);
    }

    public void updateQueue(){
        List<Playlist> pendingPlaylists = playlistRepository.getPlaylistBySongStatus(SongStatus.WAITING);
        log.debug("[runScheduledTask] - Scheduled task is executed. " +
                "Total pending playlists: {}",pendingPlaylists.size());
        pendingPlaylists.forEach( pl -> {
            if (!isPlaylistQueued(pl.getSpotifyId())) {
                Set<Song> songsToSeek = removeSongsThatNotReachMinimumWaitingTime(pl);
                log.debug("[runScheduledTask] - Playlist with id ({}) is NOT in the work queue " +
                        "and will be added",pl.getSpotifyId());
                songsToSeek.forEach(song -> song.setSearchId(UUID.randomUUID()));
                seek(pl,songsToSeek);
            } else {
                log.debug("[runScheduledTask] - Playlist with id ({}) is already in the work queue " +
                        "and WON'T be added", pl.getSpotifyId());
            }
        });
    }

    public void seek(Playlist playlist,Set<Song> songsToSeek){
        if (!isPlaylistQueued(playlist.getSpotifyId())) {
            executor.execute( () -> {
                addPlaylistToQueue(playlist,songsToSeek);
                watchlistService.updateWatchlist(playlist);
            });
        }
    }

    public synchronized void addPlaylistToQueue(Playlist playlist,Set<Song> songsToSeek){
        String playlistId = playlist.getSpotifyId();
        playlistRemainingSongs.put(
                playlistId,
                songsToSeek.size()
        );
        songsToSeek.forEach(this::addSongToQueue);
        log.debug("[addPlaylistToQueue] - Playlist added to queue: {}",playlistId);
    }

    public boolean isPlaylistQueued(String playlistId){
        playlistRemainingSongs.forEach( (key,val) -> {
            log.debug("[playlistRemainingSongs] - Playlist ID: {} - Remaining: {}",key,val);
        });
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
                    "for all his queued songs",playlist.getName(),playlistId);
            playlistRemainingSongs.remove(playlistId);
        } else {
            playlistRemainingSongs.put(playlistId,updatedRemaining);
        }
    }

    private Set<Song> removeSongsThatNotReachMinimumWaitingTime(Playlist playlist){
        int minimumMinutes = getConfiguration().getMinimumMinutesBtwSongCheck();
        if (minimumMinutes <= 0) return playlist.getSongs();
        long minimumTimeInMs = TimeUnit.MINUTES.toMillis(minimumMinutes);
        Set<Song> cleanList = playlist.getSongs().stream()
                .filter(song -> {
                    boolean shouldKeep = (song.getLastCheck() == 0 || (song.getLastCheck() + minimumTimeInMs) <= System.currentTimeMillis());
                    log.debug("[removeSongsThatNotReachMinimumWaitingTime] - Song: {} | Last Check: {} | Should Keep: {}", song.getName(), song.getLastCheck(), shouldKeep);
                    return shouldKeep;
                })
                .collect(Collectors.toSet());
        log.debug("[removeSongsThatNotReachMinimumWaitingTime] - " +
                        "A total of {} songs from playlist {} with {} songs " +
                        "will be placed in the search queue",
                cleanList.size(),
                playlist.getSpotifyId(),
                playlist.getSongs().size()
        );
        return cleanList;
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

    private SoulSyncConfiguration.App getConfiguration(){
        return ConfigurationService.instance().cfg().app();
    }
}
