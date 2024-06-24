package tech.xavi.soulsync.service.process.maintenance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.integration.SlskdGatewayService;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
public class FindCompletedProcess extends MaintenanceAbstractProcess {

    @Getter private final int order = 20;
    private final SlskdRequestService slskdRequestService;
    private final SlskdGatewayService slskdGatewayService;

    @Override
    public CompletableFuture<Boolean> execute() {
        slskdRequestService
                .getSlskdCompletedDownloads()
                .forEach(slskdRequestService::setRequestToCompletedByFile);
        slskdGatewayService
                .clearDownloads();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public String getTaskName() {
        return "FIND_COMPLETED";
    }
}
