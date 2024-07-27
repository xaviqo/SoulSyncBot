package tech.xavi.soulsync.service.playlist;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListService;
import tech.xavi.soulsync.service.song.SongService;

@Service @RequiredArgsConstructor
public class RemovePlaylistService {

    private final PlaylistService playlistService;
    private final DownloadListService downloadListService;
    private final SlskdRequestService slskdRequestService;
    private final SongService songService;

    @Transactional
    public void remove(String playlistId) {
        switch (playlistService.getPlaylistType(playlistId)){
            case DISCOGRAPHY -> removeDiscography(playlistId);
            default -> removePlaylist(playlistId);
        }
    }

    private void removeDiscography(String playlistId){
        playlistService
                .findAllByParentId(playlistId)
                .forEach(pl -> removePlaylist(pl.getId()));
    }

    private void removePlaylist(String playlistId) {
        downloadListService
                .getPlaylistDownloadLists(playlistId)
                .forEach( downloadList -> {
                    slskdRequestService.deleteDownloadListsSlskdRequests(downloadList);
                    downloadListService.deleteById(downloadList.getDownloadListId());
                });
        playlistService
                .deletePlaylist(playlistId);
        songService
                .deleteOrphanSongs();
    }

}
