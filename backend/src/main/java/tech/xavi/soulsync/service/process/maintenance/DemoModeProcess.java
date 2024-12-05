package tech.xavi.soulsync.service.process.maintenance;

import lombok.Getter;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.service.configuration.DemoModeService;

import java.util.concurrent.CompletableFuture;

@Component
public class DemoModeProcess extends MaintenanceAbstractProcess {

    private final long RUN_CLEANUP_MS;
    private final DemoModeService demoModeService;
    private long lastCleanupExecutionMs;
    @Getter
    private final int order = 1;

    public DemoModeProcess(DemoModeService demoModeService) {
        RUN_CLEANUP_MS = DemoModeService.RUN_CLEANUP_HOURS * 60 * 60 * 1000;
        this.lastCleanupExecutionMs = System.currentTimeMillis();
        this.demoModeService = demoModeService;
    }

    @Override
    public CompletableFuture<Boolean> execute() {
        if (demoModeService.isDemoMode()) {
            boolean shouldRunTask = demoModeService.isLimitReached() || isCooldownForCleanupOver();
            if (shouldRunTask) demoModeService.executeCleanup();
        }
        return CompletableFuture.completedFuture(true);
    }

    private boolean isCooldownForCleanupOver() {
        long currentMs = System.currentTimeMillis();
        boolean isCdOver = (lastCleanupExecutionMs + RUN_CLEANUP_MS) <= currentMs;
        if (isCdOver) lastCleanupExecutionMs = currentMs;
        return isCdOver;
    }

}
