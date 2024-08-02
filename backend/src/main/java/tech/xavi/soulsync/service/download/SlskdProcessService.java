package tech.xavi.soulsync.service.download;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.entity.db.SpotifySong;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.process.Process;
import tech.xavi.soulsync.service.process.download.SlskdAbstractProcess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Service
public class SlskdProcessService {

    private final List<SlskdAbstractProcess> slskdProcesses;
    @Getter private final List<SlskdRequest> currentRequests;
    private final ConfigurationFieldService cfgFieldService;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final SlskdRequestService slskdRequestService;
    @Getter private AtomicReference<String> lastFailed;
    @Getter private AtomicReference<String> lastSuccess;

    public SlskdProcessService(
            List<SlskdAbstractProcess> processes,
            ConfigurationFieldService cfgFieldService,
            ThreadPoolTaskScheduler threadPoolTaskScheduler,
            SlskdRequestService slskdRequestService
    ) {
        this.slskdProcesses = processes
                .stream()
                .sorted(Comparator.comparingInt(Process::getOrder))
                .toList();
        this.currentRequests = new ArrayList<>();
        this.cfgFieldService = cfgFieldService;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.slskdRequestService = slskdRequestService;
        this.lastSuccess = new AtomicReference<>("");
        this.lastFailed = new AtomicReference<>("");
    }

    public void handleSlskdRequest(SlskdRequest slskdRequest) {
        if (slskdRequest != null) {
            currentRequests.add(slskdRequest);

            try {
                executeProcesses(slskdRequest);
            } finally {
                slskdRequestService.saveIncreasingAttempts(slskdRequest);
                currentRequests.remove(slskdRequest);
            }
        }
    }

    private void executeProcesses(SlskdRequest slskdRequest) {
        for (SlskdAbstractProcess slskdProcess : slskdProcesses) {

            StopWatch stopWatch = initProcess(slskdRequest, slskdProcess);
            boolean isSuccess = slskdProcess.execute(slskdRequest).join();
            stopWatch.stop();

            if (slskdProcess.getStatus().equals(ProcessStatus.FINDING_FILE)) {
                if (isSuccess)
                    setSuccess(slskdRequest);
                else
                    setFailed(slskdRequest);
            }

            log.trace("Finished Process [{}] " +
                            ":: Result --> {} " +
                            ":: Task --> {} " +
                            ":: Name --> {} " +
                            ":: Elapsed --> {}",
                    slskdProcess.getTaskType(),
                    (isSuccess ? "SUCCESS" : "FAILURE"),
                    slskdProcess.getTaskName(),
                    slskdRequest.getSearchInput(),
                    stopWatch.getTotalTimeSeconds() + "s"
            );

            if (!isSuccess) {
                slskdRequestService.setRequestToWaiting(slskdRequest);
                break;
            }
        }
    }

    private StopWatch initProcess(SlskdRequest request, SlskdAbstractProcess process) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        request.setStatus(process.getStatus());
        slskdRequestService.save(request);
        return stopWatch;
    }

    public synchronized boolean isRequestSlotAvailable(){
        int totalFree = getMaxSimultaneousThreads() - currentRequests.size();
        return totalFree > 0;
    }

    private int getMaxSimultaneousThreads() {
        int maxSimultaneousThreads = cfgFieldService
                .getValue(ConfigurationField.SRCH_MAX_SIMULTANEOUS_THREADS)
                .asInt();
        if (maxSimultaneousThreads > 5 && threadPoolTaskScheduler.getPoolSize() != maxSimultaneousThreads) {
            threadPoolTaskScheduler.setPoolSize(maxSimultaneousThreads);
            threadPoolTaskScheduler.initialize();
        }
        return maxSimultaneousThreads;
    }

    private void setSuccess(SlskdRequest slskdRequest) {
        lastSuccess.set(getNameFromRequest(slskdRequest));
    }

    private void setFailed(SlskdRequest slskdRequest) {
        lastFailed.set(getNameFromRequest(slskdRequest));
    }

    private String getNameFromRequest(SlskdRequest slskdRequest) {
        SpotifySong song = slskdRequest.getSpotifySong();
        boolean hasArtistName = !song.getArtists().isEmpty();
        return song.getName()
                + (hasArtistName? " - " : "")
                + song.getFirstArtistName();
    }
}
