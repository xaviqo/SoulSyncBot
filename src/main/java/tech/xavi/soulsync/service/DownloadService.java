package tech.xavi.soulsync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadRequest;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadStatus;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResult;
import tech.xavi.soulsync.entity.DownloadStatus;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.repository.SongRepository;

import java.util.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class DownloadService {

    private final FileFinderService fileFinderService;
    private final SearchService searchService;
    private final AuthService authService;
    private final SlskdGateway slskdGateway;
    private final SongRepository songRepository;

    public void prepareDownload(Song song) {
        SlskdSearchResult[] results = searchService.fetchResults(song);
        int totalResults = results.length;
        if (totalResults > 0) {
            SlskdDownloadRequest downloadRequest = fileFinderService
                    .createDownloadRequest(song,results);
            if (downloadRequest != null){
                String filename = downloadRequest.payload().filename();
                long size = downloadRequest.payload().size();
                song.setFilename(filename);
                song.setFound(true);
                song.setSize(size);
                log.debug("[prepareDownload] - File found ({}) for this search input ({})",
                        filename,song.getSearchInput()
                );
                sendDownload(downloadRequest);
                searchService.deleteSearchFromSlskd(song);
                return;
            }
            log.debug("[prepareDownload] - Results found ({}) for this search input ({}), " +
                            "but none of them match the requested criteria",
                    totalResults,song.getSearchInput()
            );
        }
        resetNotFoundForNextIteration(song);
    }

    private void sendDownload(SlskdDownloadRequest request){
        log.debug("[sendDownload] - Song sent for download: {}",request);
        String token = authService.getSlskdToken().token();
        slskdGateway.download(request,token);
    }

    private void resetNotFoundForNextIteration(Song song){
        log.debug("[resetNotFoundForNextIteration] - Unsuccessful search. Held for next iteration: {}",song.getSearchInput());
        song.setFilename(null);
        song.setSearchId(null);
        song.setFound(false);
        song.addAttempt();
    }

    public void resetStuckSongs(){
        getSongsToBeRestartedByUsername()
                .forEach( (user, songList) -> {
                    songList.forEach( song -> {
                        String searchId = song.getSearchId().toString();
                        deleteDownload(user,searchId);
                        resetSongForDownload(song);
                    });
                });
    }

    private Map<String,List<Song>> getSongsToBeRestartedByUsername(){
        Map<String,List<Song>> songsToBeRestartedByUsername = new HashMap<>();
        String token = authService.getSlskdToken().token();
        Arrays.stream(slskdGateway.getDownloadsStatus(token))
                .forEach( status -> {
                    String username = status.getUsername();
                    List<Song> songsToBeRestarted = getSongsToBeRestarted(status.getDirectories());
                    if (!songsToBeRestarted.isEmpty()){
                        songsToBeRestartedByUsername.put(
                                username,
                                songsToBeRestarted
                        );
                    }
                });
        return songsToBeRestartedByUsername;
    }

    private List<Song> getSongsToBeRestarted(List<SlskdDownloadStatus.SlskdDirectory> directories) {
        List<Song> songsToBeRestarted = new ArrayList<>();
        directories.forEach( dir -> {
            dir.getFiles().forEach( file -> {
                String downloadStatus = file.getState();
                if (!isDownloadStatusComplete(downloadStatus)) {
                    String searchId = file.getId();
                    Song song = songRepository.findBySearchId(UUID.fromString(searchId));
                    songsToBeRestarted.add(song);
                }
            });
        });
        return songsToBeRestarted;
    }

    private boolean isDownloadStatusComplete(String status){
        return status.contains(DownloadStatus.SUCCEDED.name())
                || status.contains(DownloadStatus.QUEDED.name());
    }

    private void deleteDownload(String username, String searchId){
        String token = authService.getSlskdToken().token();
        slskdGateway.deleteDownload(token,username,searchId);
    }

    private void resetSongForDownload(Song song){
        song.setFound(false);
        songRepository.save(song);
    }

}
