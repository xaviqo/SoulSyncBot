package tech.xavi.soulsync.service.config;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.repository.AccountRepository;
import tech.xavi.soulsync.repository.PlaylistRepository;
import tech.xavi.soulsync.repository.SongRepository;
import tech.xavi.soulsync.service.auth.AccountService;

import java.io.File;
import java.time.LocalTime;
import java.util.Map;

@Log4j2
@Service
public class DemoService {

    private final boolean IS_DEMO;
    private final long STAMP_THRESHOLD;
    private final String SLSKD_DW_DIR;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public DemoService(
            @Value("${tech.xavi.soulsync.demo-mode.enabled}") String demoEnabled,
            @Value("${tech.xavi.soulsync.demo-mode.stamp-threshold}") String stampTh,
            @Value("${tech.xavi.soulsync.demo-mode.downloads-directory}") String dwDir,
            PlaylistRepository playlistRepository,
            SongRepository songRepository,
            AccountRepository accountRepository,
            AccountService accountService
    ) {
        this.IS_DEMO = demoEnabled.equals("true");
        this.STAMP_THRESHOLD = Long.parseLong(stampTh);
        this.SLSKD_DW_DIR = dwDir;
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void demoModeRestart() {
        if (!IS_DEMO) return;

        resetAccounts();
        log.info("[demoModeRestart] - Accounts restarted for demo mode");

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

    private void resetAccounts() {
        accountRepository.deleteAll();
        accountService.createDefaultAdmin();
    }

    @Transactional
    protected int removeSongsFromPl(){
        return songRepository.deleteByAdded(STAMP_THRESHOLD);
    }

    @Transactional
    protected int removePlaylists(){
        return playlistRepository.deleteByAdded(STAMP_THRESHOLD);
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
