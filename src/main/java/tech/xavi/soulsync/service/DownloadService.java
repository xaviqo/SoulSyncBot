package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.SlskdDownloadRequest;
import tech.xavi.soulsync.dto.gateway.SlskdSearchResult;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.model.Playlist;
import tech.xavi.soulsync.model.Song;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
public class DownloadService {

    private final FileFinderService fileFinderService;
    private final SearchService searchService;
    private final WatchlistService watchlistService;
    private final AuthService authService;
    private final SlskdGateway slskdGateway;
    private final ExecutorService threadExecutor;


    public DownloadService(
            FileFinderService fileFinderService,
            SearchService searchService,
            WatchlistService watchlistService,
            AuthService authService,
            SlskdGateway slskdGateway,
            @Value("${tech.xavi.soulsync.cfg.max-concurrent-threads}") int maxConcurrentDownloads
            ) {
        this.threadExecutor = Executors.newFixedThreadPool(maxConcurrentDownloads);
        this.fileFinderService = fileFinderService;
        this.watchlistService = watchlistService;
        this.authService = authService;
        this.slskdGateway = slskdGateway;
        this.searchService = searchService;
    }

    public void downloadPlaylist(Playlist playlist){
        try {
            CountDownLatch latch = new CountDownLatch(playlist.getSongs().size());
            playlist.getSongs().forEach(song -> {
                threadExecutor.execute(() -> {
                    prepareDownload(song);
                    latch.countDown();
                });
            });
            latch.await();
            watchlistService.updateWatchlist(playlist);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void prepareDownload(Song song) {
        SlskdSearchResult[] results = searchService.fetchResults(song);
        int totalResults = results.length;
        if (totalResults > 1) {
            SlskdDownloadRequest downloadRequest = fileFinderService
                    .findProperFile(song,results);
            if (downloadRequest != null){
                String filename = downloadRequest.payload().filename();
                long size = downloadRequest.payload().size();
                song.setFilename(filename);
                song.setFound(true);
                song.setSize(size);
                log.debug("[prepareDownload] - File found ({}) for this search input ({})",filename,song.getSearchInput());
                sendDownload(downloadRequest);
                return;
            }
            log.debug("[prepareDownload] - Results found ({}) for this search input ({}), but none of them match the requested criteria",totalResults,song.getSearchInput());
        }
        resetSongForNextIteration(song);
    }

    private void sendDownload(SlskdDownloadRequest request){
        log.debug("[sendDownload] - Song sent for download: {}",request);
        String token = authService.getSlskdToken().token();
        slskdGateway.download(request,token);
    }

    private void resetSongForNextIteration(Song song){
        log.debug("[resetSongForNextIteration] - Unsuccessful search. Held for next iteration: {}",song.getSearchInput());
        song.setFilename(null);
        song.setSearchId(null);
        song.setFound(false);
        song.addAttempt();
    }




}
