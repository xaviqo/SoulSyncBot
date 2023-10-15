package tech.xavi.soulsync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.SlskdDownloadRequest;
import tech.xavi.soulsync.dto.gateway.SlskdSearchResult;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.model.Song;

@Log4j2
@RequiredArgsConstructor
@Service
public class DownloadService {

    private final FileFinderService fileFinderService;
    private final SearchService searchService;
    private final AuthService authService;
    private final SlskdGateway slskdGateway;

    public void prepareDownload(Song song) {
        SlskdSearchResult[] results = searchService.fetchResults(song);
        int totalResults = results.length;
        if (totalResults > 1) {
            SlskdDownloadRequest downloadRequest = fileFinderService
                    .createDownloadRequest(song,results);
            if (downloadRequest != null){
                String filename = downloadRequest.payload().filename();
                long size = downloadRequest.payload().size();
                song.setFilename(filename);
                song.setFound(true);
                song.setSize(size);
                log.debug("[prepareDownload] - File found ({}) for this search input ({})",filename,song.getSearchInput());
                sendDownload(downloadRequest);
                searchService.deleteSearchFromSlskd(song);
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
