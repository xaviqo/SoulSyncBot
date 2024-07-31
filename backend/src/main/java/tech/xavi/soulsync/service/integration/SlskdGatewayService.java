package tech.xavi.soulsync.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.dto.gateway.GatewayToken;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdDownloadStatusResult;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchRequest;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdSearchResponse;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.repository.gateway.SlskdGateway;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service @RequiredArgsConstructor
public class SlskdGatewayService {

    private final GatewayTokenService gatewayTokenService;
    private final SlskdGateway slskdGateway;
    private final ConfigurationFieldService configurationFieldService;

    public void initSearch(SlskdSearchRequest slskdSearchRequest) {
        slskdGateway
                .baseUrl(getBaseUrl())
                .search(getToken(),slskdSearchRequest);

    }

    public void deleteSearch(SlskdRequest slskdRequest) {
        slskdGateway
                .baseUrl(getBaseUrl())
                .deleteSearch(getToken(),slskdRequest);
    }

    public void initDownload(SlskdRequest slskdRequest) {
        slskdGateway
                .baseUrl(getBaseUrl())
                .sendDownload(getToken(),slskdRequest);
    }

    public void clearDownloads(){
        slskdGateway.clearDownloads(getToken());
    }

    public boolean isSearchFinished(SlskdSearchRequest searchRequest) {
       return slskdGateway
                .baseUrl(getBaseUrl())
                .getSearchStatus(getToken(),searchRequest.id())
                .isComplete();
    }

    public Stream<SlskdDownloadStatusResult> getSlskdDownloads() {
        return Stream.of(slskdGateway
                .baseUrl(getBaseUrl())
                .getDownloadsStatus(getToken()));
    }

    public List<SlskdSearchResponse> getSearchResults(SlskdRequest slskdRequest) {
        return slskdGateway
                .baseUrl(getBaseUrl())
                .getSearchResults(getToken(),slskdRequest.getSearchId().toString())
                .responses();
    }

    public void waitForSlskdReboot() {
        do {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IllegalStateException ignored) {
            }
        } while (!isSlskdAlive());
    }

    public boolean isSlskdAlive() {
        return slskdGateway
                .baseUrl(getBaseUrl())
                .isSlskdAlive();
    }

    public void rebootSlskd() {
        slskdGateway
                .baseUrl(getBaseUrl())
                .rebootSlskd(getToken());
    }

    private GatewayToken getToken(){
        return gatewayTokenService
                .getToken(GatewayName.SLSKD);
    }

    public String getBaseUrl(){
        return configurationFieldService
                .getValue(ConfigurationField.SLSKD_API_URL)
                .asText();
    }

}
