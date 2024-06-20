package tech.xavi.soulsync.service.process.maintenance;

import lombok.Getter;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.service.process.Process;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Getter
public abstract class MaintenanceAbstractProcess implements Process {

    @Override
    public CompletableFuture<Boolean> execute() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getTaskType() {
        return "MAINTENANCE";
    }

    public String getTaskName(){
        return Objects.isNull(getStatus())
                ? "UNDEFINED"
                : getStatus().name();
    }

    public ProcessStatus getStatus(){
        return null;
    }

}
