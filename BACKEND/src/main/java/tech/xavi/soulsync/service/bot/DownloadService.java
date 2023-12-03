package tech.xavi.soulsync.service.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadRequest;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadStatus;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResult;
import tech.xavi.soulsync.entity.sub.DownloadStatus;
import tech.xavi.soulsync.entity.Song;
import tech.xavi.soulsync.entity.sub.SongStatus;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.auth.AuthService;

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
        searchService.deleteSearchFromSlskd(song);
        if (totalResults > 0) {
            songRepository.save(song);
            SlskdDownloadRequest downloadRequest = fileFinderService
                    .createDownloadRequest(song,results);
            if (downloadRequest != null){
                String filename = downloadRequest.payload().filename();
                long size = downloadRequest.payload().size();
                song.setFilename(filename);
                song.setSize(size);
                log.debug("[prepareDownload] - File found ({}) for this search input ({})",
                        filename,song.getSearchInput()
                );
                sendDownload(downloadRequest);
                song.setStatus(SongStatus.DOWNLOADING);
                songRepository.save(song);
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
        song.setStatus(SongStatus.WAITING);
    }

    public void updateSongsStatus(){
        log.debug("[updateSongsStatus] - Updating songs status...");
        List<Song> songsToBeRestarted = new ArrayList<>();
        String token = authService.getSlskdToken().token();
        Arrays.stream(slskdGateway.getDownloadsStatus(token))
                .forEach( status -> {
                    String username = status.getUsername();
                    List<Song> songToAdd = updateStatusAndGetUnfinished(
                            status.getDirectories(),
                            username
                    );
                    if (!songToAdd.isEmpty()){
                        songsToBeRestarted.addAll(songToAdd);
                    }
                });
        log.debug("[updateSongsStatus] - Found a total of {} downloads stuck",songsToBeRestarted.size());
        songsToBeRestarted.forEach(this::resetSongForDownload);
    }

    private List<Song> updateStatusAndGetUnfinished(
            List<SlskdDownloadStatus.SlskdDirectory> directories,
            String username
    ) {
        List<Song> songsToBeRestarted = new ArrayList<>();
        directories.forEach( dir -> {
            dir.getFiles().forEach( file -> {
                List<Song> songs = songRepository.findSongsByFilename(file.getFilename());
                boolean moreThanOneSongLinked = songs.size() > 1;
                if (moreThanOneSongLinked) {
                    log.warn("[updateSongsStatus] - A total of {} songs (with different Spotify IDs) " +
                            "have been found with the same linked file",songs.size());
                    log.warn("[updateSongsStatus] - File: {}",file.getFilename());
                }
                songs.forEach( song -> {
                    if (song != null) {
                        if (moreThanOneSongLinked) {
                            log.warn("[updateSongsStatus] - Song with same file: {} - {}",
                                    song.getName(),
                                    song.getArtists()[0]
                            );
                        }
                        if (file.getState().contains(DownloadStatus.SUCCEDED.getProgress())) {
                            song.setStatus(SongStatus.COMPLETED);
                            songRepository.save(song);
                        } else if (!file.getState().contains(DownloadStatus.QUEDED.getProgress())) {
                            songsToBeRestarted.add(song);
                            deleteDownload(username,file.getId());
                        }
                    }
                });
            });
        });
        return songsToBeRestarted;
    }

    private void deleteDownload(String username, String downloadId){
        String token = authService.getSlskdToken().token();
        slskdGateway.deleteDownload(token,username,downloadId);
    }

    private void resetSongForDownload(Song song){
        log.debug("[resetSongForDownload] - Search restarted due to stuck download: {} - {}",song.getName(),song.getArtists()[0]);
        song.setSize(0);
        song.setStatus(SongStatus.WAITING);
        songRepository.save(song);
    }

}
