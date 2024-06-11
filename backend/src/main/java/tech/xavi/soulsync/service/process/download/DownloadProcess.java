package tech.xavi.soulsync.service.process.download;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.download.SlskdRequestService;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class DownloadProcess extends SlskdProcess {

    private final SlskdRequestService slskdRequestService;
    @Getter private final int order = 30;

    @Override
    public CompletableFuture<Boolean> execute(SlskdRequest request) {
        slskdRequestService.sendRequest(request);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public ProcessStatus getStatus()  {
        return ProcessStatus.DOWNLOADING;
    }


}
