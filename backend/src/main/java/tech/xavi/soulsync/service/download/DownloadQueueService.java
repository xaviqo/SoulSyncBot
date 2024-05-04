package tech.xavi.soulsync.service.download;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListService;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DownloadQueueService {

    private static final int RUN_RATE_SEC = 5;
    private final ConfigurationFieldService cfgFieldService;
    private final DownloadListService downloadListService;
    private final DownloadManagerService downloadManagerService;
    private final ConcurrentLinkedQueue<SlskdRequest> queue;
    private final SlskdRequestService slskdRequestService;

    public DownloadQueueService(
            ConfigurationFieldService cfgFieldService,
            DownloadListService downloadListService,
            DownloadManagerService downloadManagerService,
            SlskdRequestService slskdRequestService
    ) {
        this.cfgFieldService = cfgFieldService;
        this.downloadListService = downloadListService;
        this.downloadManagerService = downloadManagerService;
        this.queue = new ConcurrentLinkedQueue<>();
        this.slskdRequestService = slskdRequestService;
    }

    @Async
    @Scheduled(fixedRate = RUN_RATE_SEC * 1000)
    protected void runQueue(){
        if (isRequestSlotAvailable()) {
            downloadListService
                    .getNextDownloadList()
                    .ifPresent( downloadList -> {
                        if (queue.isEmpty()) {
                            updateQueue(downloadList);
                        }
                        downloadManagerService
                                .handleSlskdRequest(queue.poll());
                        if (isDownloadListCompleted(downloadList)) {
                            updateDownloadList(downloadList);
                        }
                    });
        }

    }

    private void updateQueue(DownloadList downloadList) {
        int queueLimit = cfgFieldService
                .getValue(ConfigurationField.APP_TRACKS_PER_QUEUE)
                .asInt();
        Set<SlskdRequest> nextQueue = slskdRequestService
                .getSongsQueue(downloadList)
                .limit(queueLimit)
                .collect(Collectors.toSet());
        queue.addAll(nextQueue);
    }

    private void updateDownloadList(DownloadList downloadList){
        downloadList.increaseAttempts();
        downloadListService.save(downloadList);
    }

    private boolean isRequestSlotAvailable() {
        return downloadManagerService
                .isRequestSlotAvailable();
    }

    private boolean isDownloadListCompleted(DownloadList downloadList){
        return slskdRequestService
                .getSongsQueue(downloadList)
                .toList()
                .isEmpty();
    }

}
