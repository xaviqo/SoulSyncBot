package tech.xavi.soulsync.service.configuration;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.Role;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.Playlist;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListService;
import tech.xavi.soulsync.service.playlist.PlaylistService;
import tech.xavi.soulsync.service.playlist.RemovePlaylistService;

import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class DemoModeService {

    private static final String DEMO_LOGGER_PREFIX = "[DEMO_MODE] ~ ";
    public static final int RUN_CLEANUP_HOURS = 24;
    private static final int MAX_DOWNLOAD_LISTS_IN_DEMO = 2;
    private final boolean IS_DEMO_MODE;
    private final DownloadListService downloadListService;
    private final PlaylistService playlistService;
    private final RemovePlaylistService removePlaylistService;

    public DemoModeService(
            ConfigurationFieldService configurationFieldService,
            DownloadListService downloadListService,
            PlaylistService playlistService,
            RemovePlaylistService removePlaylistService
    ) {
        this.IS_DEMO_MODE = configurationFieldService
                .getFieldWithValue(ConfigurationField.IS_DEMO_MODE,false)
                .getValue()
                .asText()
                .equalsIgnoreCase("true");
        this.downloadListService = downloadListService;
        this.playlistService = playlistService;
        this.removePlaylistService = removePlaylistService;
    }

    @Transactional
    public void executeCleanup() {
        log.info("{}Executing cleanup task",DEMO_LOGGER_PREFIX);
        List<Playlist> playlistsToDelete = playlistService
                .findAll()
                .stream()
                .filter(this::shouldPlaylistBeDeleted)
                .toList();
        log.info("{}Found to delete a total of {} playlists",DEMO_LOGGER_PREFIX,playlistsToDelete.size());
        playlistsToDelete
                .forEach( pl -> {
                    Hibernate.initialize(pl.getDownloadLists());
                    Hibernate.initialize(pl.getSubPlaylists());
                    Hibernate.initialize(pl.getParentPlaylist());
                    log.info("{}Deleting -> {}",DEMO_LOGGER_PREFIX,pl);
                    removePlaylistService.remove(pl.getId());
        });
        log.info("{}Finished cleanup task",DEMO_LOGGER_PREFIX);
    }

    public boolean isLimitReached() {
        long totalDls = downloadListService.countTotal();
        boolean isLimitReached = totalDls > MAX_DOWNLOAD_LISTS_IN_DEMO;
        if (isLimitReached)
            log.info("{}Total of download lists in demo mode has been exceeded [{} > {}] - " +
                            "Cleanup task will be executed prematurely",
                    DEMO_LOGGER_PREFIX,
                    totalDls,
                    MAX_DOWNLOAD_LISTS_IN_DEMO
            );
        return isLimitReached;
    }

    public boolean isDemoMode() {
        return IS_DEMO_MODE;
    }

    private boolean shouldPlaylistBeDeleted(Playlist playlist) {
        return !Objects.equals(playlist.getOwnerRole(), Role.ADMIN.name());
    }
}
