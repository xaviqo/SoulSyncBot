package tech.xavi.soulsync.service.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.process.maintenance.MaintenanceAbstractProcess;

import java.util.Comparator;
import java.util.List;

@Log4j2
@Service
public class MaintenanceProcessManagerService {

    private static final int RUN_INTERVAL_SEC = 60;
    private final List<MaintenanceAbstractProcess> maintenanceProcesses;
    private final ConfigurationFieldService configurationFieldService;
    private long lastExecutionMs;

    public MaintenanceProcessManagerService(
            List<MaintenanceAbstractProcess> processes,
            ConfigurationFieldService cfgFieldService)
    {
        this.lastExecutionMs = System.currentTimeMillis();
        this.maintenanceProcesses = processes
                .stream()
                .sorted(Comparator.comparingInt(Process::getOrder))
                .toList();
        this.configurationFieldService = cfgFieldService;
    }

    @Scheduled(fixedRate = RUN_INTERVAL_SEC * 1000, initialDelay = RUN_INTERVAL_SEC * 1000)
    protected void runMaintenance() {
        if (shouldRunTask() && isCooldownOver()) {
            for (MaintenanceAbstractProcess maintenanceProcess : maintenanceProcesses) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                maintenanceProcess.execute().join();
                stopWatch.stop();

                log.trace("Finished Process [{}] " +
                                ":: Task --> {} " +
                                ":: Elapsed --> {}",
                        maintenanceProcess.getTaskType(),
                        maintenanceProcess.getTaskName(),
                        stopWatch.getTotalTimeSeconds() + "s"
                );
            }
        }
    }

    private boolean isCooldownOver() {
        long currentMs = System.currentTimeMillis();
        long coolDownMs = configurationFieldService
                .getValue(ConfigurationField.APP_MAINTENANCE_TASK_INTERVAL_MINS)
                .asLong() * 60 * 1000;
        boolean isExpired = lastExecutionMs + coolDownMs <= currentMs;
        if (isExpired) lastExecutionMs = currentMs;
        return isExpired;
    }

    private boolean shouldRunTask() {
        boolean isInstalled = configurationFieldService
                .getValue(ConfigurationField.IS_APP_INSTALLED)
                .asBoolean();
        boolean shouldRun = configurationFieldService
                .getValue(ConfigurationField.APP_RUN_MAINTENANCE_TASK)
                .asBoolean();
        return isInstalled && shouldRun;
    }

}
