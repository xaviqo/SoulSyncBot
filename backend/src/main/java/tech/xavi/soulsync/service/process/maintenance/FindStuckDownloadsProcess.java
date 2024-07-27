package tech.xavi.soulsync.service.process.maintenance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.download.SlskdRequestService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class FindStuckDownloadsProcess extends MaintenanceAbstractProcess {

    @Getter private final int order = 30;
    private final SlskdRequestService slskdRequestService;
    private final ConfigurationFieldService configurationFieldService;

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
