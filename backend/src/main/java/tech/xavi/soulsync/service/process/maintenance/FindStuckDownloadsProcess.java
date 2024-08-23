package tech.xavi.soulsync.service.process.maintenance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.stats.StatsService;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class FindStuckDownloadsProcess extends MaintenanceAbstractProcess {

    @Getter private final int order = 20;
    private final SlskdRequestService slskdRequestService;
    private final ConfigurationFieldService configurationFieldService;
    private final StatsService statsService;
    private static final long ONE_HOUR_IN_MS = 60 * 60 * 1000;
    private static final ProcessStatus[] STATUSES_TO_CHECK_STUCK = new ProcessStatus[]{
            ProcessStatus.SEARCHING,
            ProcessStatus.FINDING_FILE,
            ProcessStatus.DOWNLOADING
    };

    @Override
    public CompletableFuture<Boolean> execute() {
        slskdRequestService
                .getAllStuckDownloads()
                .forEach( download ->
                        slskdRequestService
                                .findByFilenameAndUser(download)
                                .ifPresent( req -> {
                                    if (hasPastStuckThreshold(req))
                                        slskdRequestService.setRequestToWaiting(req);
                                })
                );
        slskdRequestService
                .findByStatus(STATUSES_TO_CHECK_STUCK)
                .forEach( slskdRequest -> {
                    boolean isStatusStuck = Instant.now().toEpochMilli() - slskdRequest.getLastCheck() >= ONE_HOUR_IN_MS;
                    if (isStatusStuck) slskdRequestService.setRequestToWaiting(slskdRequest);
                });

        if (statsService.isBanned()) {
            slskdRequestService
                    .findByStatus(ProcessStatus.SEARCHING)
                    .forEach(slskdRequestService::setRequestToWaiting);
        }
        return CompletableFuture.completedFuture(true);
    }

    private boolean hasPastStuckThreshold(SlskdRequest request){
        return (request.getAdded() + getMinutesToEvaluateStuck())
                > System.currentTimeMillis();
    }

    private long getMinutesToEvaluateStuck() {
        return TimeUnit.MINUTES
                .toMillis(
                        configurationFieldService
                                .getValue(ConfigurationField.SRCH_MINUTES_TO_EVALUATE_AS_STUCK)
                                .asLong()
                );
    }

    @Override
    public String getTaskName() {
        return "FIND_STUCK";
    }
}
