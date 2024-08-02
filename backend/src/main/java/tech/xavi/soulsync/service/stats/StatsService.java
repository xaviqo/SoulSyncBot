package tech.xavi.soulsync.service.stats;

import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.stats.*;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.download.SlskdProcessService;
import tech.xavi.soulsync.service.download.SlskdRequestService;
import tech.xavi.soulsync.service.search.FileFinderService;
import tech.xavi.soulsync.service.throttle.SlskdRequestsThrottleService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final ConfigurationFieldService configurationFieldService;
    private final SlskdRequestsThrottleService throttleService;
    private final SlskdProcessService slskdProcessService;
    private final SlskdRequestService slskdRequestService;
    private final FileFinderService fileFinderService;
    private final LocalDateTime startTime;

    public StatsService(ConfigurationFieldService configurationFieldService,
                        SlskdRequestsThrottleService throttleService,
                        SlskdProcessService slskdProcessService,
                        SlskdRequestService slskdRequestService,
                        FileFinderService fileFinderService
    ) {
        this.startTime = LocalDateTime.now();
        this.configurationFieldService = configurationFieldService;
        this.throttleService = throttleService;
        this.slskdProcessService = slskdProcessService;
        this.slskdRequestService = slskdRequestService;
        this.fileFinderService = fileFinderService;
    }

    public List<SlskdQueueReqDto> getCurrentQueueRequests() {
        return slskdProcessService
                .getCurrentRequests()
                .stream()
                .map( req -> SlskdQueueReqDto.builder()
                        .name(req.getSpotifySong().getName())
                        .artists(req.getSpotifySong().getArtists())
                        .status(req.getStatus())
                        .playlistCover(req.getPlaylist().getCover())
                        .searchInput(req.getSearchInput())
                        .lastCheck(req.getLastCheck())
                        .attempts(req.getAttempts())
                        .build() )
                .toList();
    }

    public IterationStats getIterationsStats() {
        boolean isBanned = Objects
                .nonNull(throttleService.getBanExpirationTime());
        LocalDateTime banExpTime = isBanned
                ? throttleService.getBanExpirationTime()
                : LocalDateTime.now();

        return IterationStats.builder()
                .banExpirationTime(banExpTime.atZone(ZoneId.systemDefault()).toEpochSecond())
                .nextIteration(throttleService.getNextIteration().atZone(ZoneId.systemDefault()).toEpochSecond())
                .lastBans(throttleService.getLastBans())
                .lastIterations(throttleService.getLastIterations())
                .currentRequests(throttleService.getCurrentRequests())
                .isBanned(isBanned)
                .throttleMultiplier(throttleService.getThrottleMultiplier())
                .adjustedMillisBetweenRequests(throttleService.getMillisBetweenRequests())
                .millisBetweenRequests(configurationFieldService
                        .getValue(ConfigurationField.SRCH_MILLIS_BETWEEN_REQUESTS)
                        .asLong())
                .minsUntilBanExpiry(Duration.between(
                        LocalDateTime.now(),
                        banExpTime
                ).toMinutes())
                .build();
    }

    public ProcessSummaryDto getProcessSummary() {
        AtomicReference<String> lastSuccess = slskdProcessService
                .getLastSuccess();
        AtomicReference<String> lastFailed = slskdProcessService
                .getLastFailed();
        long totalSuccess = slskdRequestService.countByStatuses(
                1,
                ProcessStatus.COPIED,
                ProcessStatus.COMPLETED,
                ProcessStatus.DOWNLOADING
        );
        long totalFailed = slskdRequestService.countByStatuses(
                1,
                ProcessStatus.WAITING
        );
        long totalProcessed = slskdRequestService.countByStatuses(
                1,
                ProcessStatus.values()
        );

        return ProcessSummaryDto.builder()
                .runningTime(getRunningTimeStr())
                .totalProcessed(totalProcessed)
                .totalSuccess(totalSuccess)
                .totalFailed(totalFailed)
                .lastSuccess(lastSuccess.get())
                .lastFailed(lastFailed.get())
                .build();
    }

    public FindingLogicDto getFindingLogicStats() {
        return FindingLogicDto.builder()
                .totalFlexible(fileFinderService.getTotalFoundFlexible().get())
                .totalStrict(fileFinderService.getTotalFoundStrict().get())
                .build();
    }

    public Map<ProcessStatus, Long> countByStatus() {
        return slskdRequestService
                .countByStatus()
                .stream()
                .collect(Collectors.toMap(StatusCountDto::status,StatusCountDto::total));
    }

    private String getRunningTimeStr() {
        Duration elapsed = Duration.between(startTime,LocalDateTime.now());
        long seconds = elapsed.getSeconds() % 60;
        long minutes = elapsed.toMinutes() % 60;
        long hours = elapsed.toHours();
        return hours + "h, " + minutes + "m, " + seconds + "s.";
    }

}
