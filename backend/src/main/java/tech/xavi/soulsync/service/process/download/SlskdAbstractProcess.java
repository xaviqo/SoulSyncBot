package tech.xavi.soulsync.service.process.download;

import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.process.Process;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class SlskdAbstractProcess implements Process {

    public CompletableFuture<Boolean> execute(SlskdRequest slskdRequest) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> execute() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getTaskType() {
        return "SLSKD";
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
