package tech.xavi.soulsync.service.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.service.task.QueueManagerService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class QueueRestService {

    private final QueueManagerService queueManagerService;

    public void pausePlaylist(String playlistId, boolean pause){
        if (pause) {
            queueManagerService.pausePlaylist(playlistId);
        } else {
            queueManagerService.unpausePlaylist(playlistId);
        }
    }

    public Map<String, Object> isPlaylistPaused(String playlistId){
        Map<String,Object> response = new HashMap<>();
        response.put("playlistId",playlistId);
        response.put("isPaused",queueManagerService.isPlaylistPaused(playlistId));
        return response;
    }
}
