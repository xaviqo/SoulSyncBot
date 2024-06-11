package tech.xavi.soulsync.service.process.download;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchRequest;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.search.SearchService;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class SearchProcess extends SlskdProcess {

    private final SearchService searchService;
    @Getter private final int order = 10;

    @Override
    public CompletableFuture<Boolean> execute(SlskdRequest request) {
        searchService.updateLastCheck(request);
        SlskdSearchRequest searchRequest = searchService.initSearch(request);
        boolean isSuccess = searchService.waitUntilSearchIsFinished(searchRequest);
        return CompletableFuture.completedFuture(isSuccess);
    }

    @Override
    public ProcessStatus getStatus()  {
        return ProcessStatus.SEARCHING;
    }


}
