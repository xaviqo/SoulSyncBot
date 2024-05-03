package tech.xavi.soulsync.service.task.download;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.datafile.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdDownload;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.playlist.downloadlist.DownloadListMainService;
import tech.xavi.soulsync.service.song.SlskdDownloadsService;
import tech.xavi.soulsync.service.task.Process;
import tech.xavi.soulsync.service.task.download.each.AbstractSlskdProcess;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Service
public class DownloadManagerService {

    private static final int RUN_RATE_SEC = 30;
    private final ConfigurationFieldService cfgFieldService;
    private final DownloadListMainService downloadListService;
    private final SlskdDownloadsService slskdDownloadsService;
    private final Set<AbstractSlskdProcess> processes;
    private final LinkedList<SlskdDownload> songQueue;
    private DownloadList currentDownloadList;

    public DownloadManagerService(
            Set<AbstractSlskdProcess> processes,
            ConfigurationFieldService cfgFieldService,
            DownloadListMainService downloadListService,
            SlskdDownloadsService slskdDownloadsService
    ) {
        this.processes = new CopyOnWriteArraySet<>(processes);
        this.cfgFieldService = cfgFieldService;
        this.downloadListService = downloadListService;
        this.slskdDownloadsService = slskdDownloadsService;
        this.songQueue = new LinkedList<>();
    }

    @Scheduled(fixedRate = RUN_RATE_SEC * 1000)
    protected void run() {
        completeUpdate(false);
        processes.parallelStream().forEach( process -> {
            try {
                long processElapsed = process.execute().get();
                log.debug("Finished Download Process " +
                                ":: List --> {} " +
                                ":: Search Input --> {} " +
                                ":: Time elapsed --> {}",
                        process.getDownloadList().getPlaylistId(),
                        process.getSlskdDownload().getSearchInput(),
                        processElapsed
                );
                slskdDownloadsService
                        .save(process.getSlskdDownload());
                completeUpdate(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateDownloadList(){
        if (songQueue.isEmpty()) {
            if (currentDownloadList != null) {
                downloadListService
                        .save(currentDownloadList);
            }
            currentDownloadList = downloadListService
                    .getNextDownloadList();
        }
    }

    private void updateQueue() {
        if (songQueue.isEmpty()) {
            Set<SlskdDownload> nextQueue = slskdDownloadsService
                    .getSongsQueue(currentDownloadList)
                    .limit(getMaxTracksPerQueue())
                    .collect(Collectors.toSet());
            songQueue.addAll(nextQueue);
        }
    }

    private void updateProcesses() {
        IntStream.range(0, getTotalFreeProcesses())
                .mapToObj(i -> DownloadProcess.builder()
                        .processes(processes)
                        .downloadList(currentDownloadList)
                        .slskdDownload(songQueue.pop())
                        .build() )
                .forEach(processes::add);
    }

    private void completeUpdate(boolean isForced){
        if (isForced || processes.isEmpty()) {
            updateDownloadList();
            updateQueue();
            updateProcesses();
        }
    }

    private int getTotalFreeProcesses(){
        return getTotalDownloadProcesses() - processes.size();
    }

    private int getTotalDownloadProcesses() {
        return cfgFieldService
                .getValue(ConfigurationField.APP_MAX_SIMULTANEOUS_PROCESSES)
                .asInt();
    }

    private int getMaxTracksPerQueue() {
        return cfgFieldService
                .getValue(ConfigurationField.APP_TRACKS_PER_QUEUE)
                .asInt();
    }

}
