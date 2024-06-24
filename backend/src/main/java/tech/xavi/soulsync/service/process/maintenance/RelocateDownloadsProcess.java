package tech.xavi.soulsync.service.process.maintenance;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.relocate.RelocationService;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class RelocateDownloadsProcess extends MaintenanceAbstractProcess {

    @Getter
    private final int order = 30;
    private final SlskdRequestService slskdRequestService;
    private final RelocationService relocationService;
    private final ConfigurationFieldService configurationFieldService;

    @Override @Transactional
    public CompletableFuture<Boolean> execute() {
        if (shouldRelocate()) {
            slskdRequestService
                    .findByStatus(ProcessStatus.COMPLETED)
                    .stream()
                    .parallel()
                    .forEach(relocationService::relocate);
        }
        return CompletableFuture.completedFuture(true);
    }

    private boolean shouldRelocate(){
        return configurationFieldService
                .getValue(ConfigurationField.APP_IS_RELOCATE)
                .asBoolean();
    }

    @Override
    public String getTaskName() {
        return "RELOCATE_DOWNLOADS";
    }
}

