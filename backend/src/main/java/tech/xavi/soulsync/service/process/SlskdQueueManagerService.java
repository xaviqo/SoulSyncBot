package tech.xavi.soulsync.service.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.DownloadList;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.download.SlskdProcessService;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.download.downloadlist.DownloadListService;
import tech.xavi.soulsync.service.search.SearchPolicyService;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Log4j2
@Service
public class SlskdQueueManagerService {

    private static final int RUN_RATE_MS = 1000;
    private final ConcurrentLinkedQueue<SlskdRequest> queue;

    private final ConfigurationFieldService cfgFieldService;
    private final DownloadListService downloadListService;
    private final SlskdProcessService slskdProcessService;
    private final SlskdRequestService slskdRequestService;
    private final SearchPolicyService searchPolicyService;

    public SlskdQueueManagerService(
            ConfigurationFieldService cfgFieldService,
            DownloadListService downloadListService,
            SlskdProcessService slskdProcessService,
            SlskdRequestService slskdRequestService,
            SearchPolicyService searchPolicyService
    ) {
        this.cfgFieldService = cfgFieldService;
        this.downloadListService = downloadListService;
        this.slskdProcessService = slskdProcessService;
        this.slskdRequestService = slskdRequestService;
        this.searchPolicyService = searchPolicyService;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Async
    @Scheduled(fixedRate = RUN_RATE_MS, initialDelay = RUN_RATE_MS)
    protected void runQueue() {
        if (shouldRunTask() && isRequestSlotAvailable())
            getNextDownloadList().ifPresent(downloadList ->
                    getNextRequestFromQueue(downloadList).ifPresent(request ->
                            handleRequestAndUpdate(downloadList, request)
                    )
            );
    }

    private Optional<DownloadList> getNextDownloadList() {
        return downloadListService
                .getDownloadLists()
                .filter(DownloadList::getIsActive)
                .filter(this::hasDownloadListRequestsWaiting)
                .min(DownloadPriority::compare);
    }

    private boolean hasDownloadListRequestsWaiting(DownloadList downloadList) {
        return slskdRequestService
                .countByDownloadListAndStatus(downloadList, ProcessStatus.WAITING) > 0;
    }

    private void handleRequestAndUpdate(DownloadList downloadList, SlskdRequest request) {
        slskdProcessService.handleSlskdRequest(request);
        if (isDownloadListCompleted(downloadList))
            updateDownloadList(downloadList);
    }

    private Optional<SlskdRequest> getNextRequestFromQueue(DownloadList downloadList) {
        if (queue.isEmpty()) updateQueue(downloadList);
        return Optional.ofNullable(queue.poll());
    }

    private void updateQueue(DownloadList downloadList) {
        int queueLimit = cfgFieldService
                .getValue(ConfigurationField.SRCH_TRACKS_PER_QUEUE)
                .asInt();
        Set<SlskdRequest> nextQueue = slskdRequestService
                .getSongsQueue(downloadList)
                .limit(queueLimit)
                .filter( req -> isRequestWaiting(req) && hasRequestPassedTimeThreshold(req) )
                .collect(Collectors.toSet());
        queue.addAll(nextQueue);
    }

    private boolean isRequestWaiting(SlskdRequest slskdRequest) {
        return slskdRequest
                .getStatus()
                .equals(ProcessStatus.WAITING);
    }

    private boolean hasRequestPassedTimeThreshold(SlskdRequest slskdRequest) {
        if (slskdRequest.getLastCheck() == 0) return true;
        long waitingMs = searchPolicyService
                .getPolicyById(slskdRequest.getDownloadList().getSearchPolicy())
                .getMinimumMinutesPerRetry() * 60L * 1000L;
        long msThreshold = slskdRequest.getLastCheck() + waitingMs;
        return msThreshold < System.currentTimeMillis();
    }

    private void updateDownloadList(DownloadList downloadList) {
        downloadList.setLastCheck(System.currentTimeMillis());
        downloadList.increaseAttempts();
        downloadListService.save(downloadList);
    }

    private boolean isRequestSlotAvailable() {
        return slskdProcessService
                .isRequestSlotAvailable();
    }

    private boolean isDownloadListCompleted(DownloadList downloadList){
        return slskdRequestService
                .getSongsQueue(downloadList)
                .filter( req -> isRequestWaiting(req) && hasRequestPassedTimeThreshold(req) )
                .toList()
                .isEmpty();
    }

    private boolean shouldRunTask() {
        return cfgFieldService
                .getValue(ConfigurationField.APP_RUN_DOWNLOAD_TASK)
                .asBoolean();
    }

}
