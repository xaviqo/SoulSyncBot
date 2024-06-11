package tech.xavi.soulsync.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchRequest;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.integration.SlskdGatewayService;

import java.util.concurrent.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class SearchService {

    private final SlskdGatewayService slskdGatewayService;
    private final ConfigurationFieldService configurationFieldService;

    public SlskdSearchRequest initSearch(SlskdRequest slskdRequest) {
        SlskdSearchRequest searchRequest = SlskdSearchRequest.builder()
                .id(slskdRequest.getSearchId().toString())
                .searchText(slskdRequest.getSearchInput())
                .build();
        slskdGatewayService
                .initSearch(searchRequest);
        return searchRequest;
    }

    public boolean waitUntilSearchIsFinished(SlskdSearchRequest searchRequest) {
        int maxWaitTimeSeconds = configurationFieldService
                .getValue(ConfigurationField.APP_SEARCH_WAITING_SECONDS)
                .asInt();
        int elapsedSeconds = 0;
        do {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for SLSKD search results", e);
                Thread.currentThread().interrupt();
                return false;
            }
            if (isSearchFinished(searchRequest))
                return true;
            elapsedSeconds++;
        } while (elapsedSeconds < maxWaitTimeSeconds);

        log.warn("Search '{}' did not finish within the allotted time ({}sec)",
                searchRequest.searchText(),
                maxWaitTimeSeconds);
        return false;
    }

    public void updateLastCheck(SlskdRequest slskdRequest) {
        slskdRequest.setLastCheck(System.currentTimeMillis());
    }

    private boolean isSearchFinished(SlskdSearchRequest searchRequest){
        return slskdGatewayService.isSearchFinished(searchRequest);
    }

}
