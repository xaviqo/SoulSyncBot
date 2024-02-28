package tech.xavi.soulsync.service.config;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.projection.PlaylistProjection;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;

import java.io.File;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Transactional
@Log4j2
@Service
public class DemoService {

    private final String CRON = "0 0 * * * *";
    private final boolean IS_DEMO;
    private final long STAMP_THRESHOLD;
    private final String SLSKD_DW_DIR;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public DemoService(
            @Value("${tech.xavi.soulsync.demo-mode.enabled}") String demoEnabled,
            @Value("${tech.xavi.soulsync.demo-mode.stamp-threshold}") String stampTh,
            @Value("${tech.xavi.soulsync.demo-mode.downloads-directory}") String dwDir,
            PlaylistRepository playlistRepository,
            SongRepository songRepository
    ) {
        this.IS_DEMO = demoEnabled.equals("true");
        this.STAMP_THRESHOLD = Long.parseLong(stampTh);
        this.SLSKD_DW_DIR = dwDir;
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        log.info("[DemoService] - Threshold for deletion: {}",STAMP_THRESHOLD);
    }

    @Scheduled(cron = CRON)
    public void demoModeRestart() {
        if (!IS_DEMO) return;

        log.info("[demoModeRestart] - Timestamp threshold for deletion: {}",STAMP_THRESHOLD);

        getDeletionCandidates().forEach( pl -> {
            log.info("[demoModeRestart] - The playlist exceeds the threshold '{}'. It will be deleted --> " +
                    "[ID: {} | NAME: {} | TYPE: {} | ADDED: {}]",
                    STAMP_THRESHOLD,
                    pl.getSpotifyId(),
                    pl.getName(),
                    pl.getType(),
                    pl.getAdded()
            );
        });

        int totalDeletedSongs = removeSongsFromPl();
        if (totalDeletedSongs > 0) {
            log.info("[demoModeRestart] - A total of {} songs have been removed from DB",totalDeletedSongs);
        }

        int totalDeletedPl = removePlaylists();
        if (totalDeletedPl > 0) {
            log.info("[demoModeRestart] - A total of {} playlists have been removed from DB",totalDeletedPl);
        }

        long totalDeletedFiles = deleteFilesFromDir();
        if (totalDeletedFiles > 0) {
            log.info("[demoModeRestart] - A total of {} downloads have been removed from disk",totalDeletedFiles);
        }

    }

    public Map<String,Object> isDemoMode(){
        int minForNextReset = 60 - LocalTime.now().getMinute();
        return Map.of(
                "isDemoMode", isDemoModeOn(),
                "nextReset", minForNextReset
        );
    }

    public boolean isDemoModeOn(){
        return IS_DEMO;
    }

    protected int removeSongsFromPl(){
        return songRepository.deleteByAdded(STAMP_THRESHOLD);
    }

    protected int removePlaylists(){
        return playlistRepository.deleteByAdded(STAMP_THRESHOLD);
    }

    protected List<PlaylistProjection> getDeletionCandidates(){
        return playlistRepository.getDeletionCandidates(STAMP_THRESHOLD);
    }

    private long deleteFilesFromDir() {
        File dwDir = new File(SLSKD_DW_DIR);
        if (dwDir.isDirectory()) {
            File[] files = dwDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.delete()) {
                        log.info("[deleteFilesFromDir] - Deleted file: {}",file.getName());
                    }
                }
                return files.length;
            } else {
                log.info("[deleteFilesFromDir] - No downloads were found in the directory: {}",SLSKD_DW_DIR);
            }
        } else {
            log.error("[deleteFilesFromDir] - The directory does not exist");
        }
        return 0;
    }

}
