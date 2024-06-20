package tech.xavi.soulsync.service.process.maintenance;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.PlaylistType;
import tech.xavi.soulsync.service.playlist.PlaylistService;
import tech.xavi.soulsync.service.playlist.UpdatePlaylistService;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class UpdatePlaylistProcess extends MaintenanceAbstractProcess {

    private final PlaylistService playlistService;
    private final UpdatePlaylistService updatePlaylistService;
    @Getter private final int order = 10;

    @Override @Transactional
    public CompletableFuture<Boolean> execute() {
        playlistService
                .findAllByType(PlaylistType.PLAYLIST)
                .forEach(updatePlaylistService::updatePlaylist);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getTaskName() {
        return "UPDATE_PLAYLIST";
    }

}
