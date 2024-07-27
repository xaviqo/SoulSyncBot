package tech.xavi.soulsync.service.process.download;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.integration.SlskdGatewayService;
import tech.xavi.soulsync.service.search.FileFinderService;

import java.util.concurrent.CompletableFuture;

@Log4j2
@RequiredArgsConstructor
@Component
public class FileFinderProcess extends SlskdAbstractProcess {

    private final SlskdGatewayService slskdGatewayService;
    private final FileFinderService fileFinderService;
    @Getter private final int order = 20;

    @Override
    public CompletableFuture<Boolean> execute(SlskdRequest request) {
        slskdGatewayService
                .getSearchResults(request)
                .stream()
                .filter( response -> !response.files().isEmpty() )
                .forEach( response -> fileFinderService
                                .getFindingModes()
                                .forEach( mode -> mode.accept(request, response) )
                );
        slskdGatewayService
                .deleteSearch(request);
        return CompletableFuture
                .completedFuture(isFound(request));
    }

    private boolean isFound(SlskdRequest slskdRequest) {
        return slskdRequest.getFilename() != null
                && !slskdRequest.getFilename().isEmpty()
                && slskdRequest.getSharedBy() != null
                && !slskdRequest.getSharedBy().isEmpty();
    }

    @Override
    public ProcessStatus getStatus()  {
        return ProcessStatus.FINDING_FILE;
    }


}
