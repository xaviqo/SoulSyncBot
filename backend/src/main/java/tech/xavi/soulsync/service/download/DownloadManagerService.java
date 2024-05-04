package tech.xavi.soulsync.service.download;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.download.process.SlskdProcess;
import tech.xavi.soulsync.service.task.Process;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Log4j2
@Service
public class DownloadManagerService {

    private final List<SlskdProcess> taskProcesses;
    private final List<SlskdRequest> currentRequests;
    private final ConfigurationFieldService cfgFieldService;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final SlskdRequestService slskdRequestService;

    public DownloadManagerService(
            List<SlskdProcess> processes,
            ConfigurationFieldService cfgFieldService,
            ThreadPoolTaskScheduler threadPoolTaskScheduler, SlskdRequestService slskdRequestService) {
        this.taskProcesses = processes
                .stream()
                .sorted(Comparator.comparingInt(Process::getOrder))
                .toList();
        this.currentRequests = new ArrayList<>();
        this.cfgFieldService = cfgFieldService;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.slskdRequestService = slskdRequestService;
    }

    public void handleSlskdRequest(SlskdRequest slskdRequest) {
        currentRequests
                .add(slskdRequest);
        taskProcesses
                .forEach( slskdProcess -> {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    slskdProcess
                            .execute(slskdRequest)
                            .whenComplete((result, throwable) -> {
                                stopWatch.stop();
                                log.debug("Finished Process [{}] " +
                                                ":: Task Type --> {} " +
                                                ":: Task Name --> {} " +
                                                ":: Time Elapsed --> {}",
                                        slskdProcess.getTaskType(),
                                        slskdProcess.getTaskName(),
                                        stopWatch.getTotalTimeSeconds()+"s",
                                        slskdRequest.getSearchInput()
                                );
                    });
        });
        slskdRequestService
                .saveIncreasingAttempts(slskdRequest);
        currentRequests
                .remove(slskdRequest);
    }

    public synchronized boolean isRequestSlotAvailable(){
        int totalFree = getMaxSimultaneousThreads() - currentRequests.size();
        log.trace("Free space in currentRequests --> {}", totalFree);
        return totalFree > 0;
    }

    private int getMaxSimultaneousThreads() {
        int maxSimultaneousThreads = cfgFieldService
                .getValue(ConfigurationField.APP_MAX_SIMULTANEOUS_THREADS)
                .asInt();
        if (maxSimultaneousThreads > 5 && threadPoolTaskScheduler.getPoolSize() != maxSimultaneousThreads) {
            threadPoolTaskScheduler.setPoolSize(maxSimultaneousThreads);
            threadPoolTaskScheduler.initialize();
        }
        return maxSimultaneousThreads;
    }
}
