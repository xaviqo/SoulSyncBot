package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.dto.stats.FindingLogicDto;
import tech.xavi.soulsync.dto.stats.IterationStats;
import tech.xavi.soulsync.dto.stats.ProcessSummaryDto;
import tech.xavi.soulsync.dto.stats.SlskdQueueReqDto;
import tech.xavi.soulsync.service.stats.StatsService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping(ApiRoutes.EP_STATS_QUEUE)
    public ResponseEntity<List<SlskdQueueReqDto>> getCurrentQueue() {
        return ResponseEntity.ok(statsService.getCurrentQueueRequests());
    }

    @GetMapping(ApiRoutes.EP_STATS_ITERATIONS)
    public ResponseEntity<IterationStats> getIterationsStats() {
        return ResponseEntity.ok(statsService.getIterationsStats());
    }

    @GetMapping(ApiRoutes.EP_STATS_SUMMARY)
    public ResponseEntity<ProcessSummaryDto> getProcessSummary() {
        return ResponseEntity.ok(statsService.getProcessSummary());
    }

    @GetMapping(ApiRoutes.EP_STATS_FINDING_LOGIC)
    public ResponseEntity<FindingLogicDto> getFindingLogicStats() {
        return ResponseEntity.ok(statsService.getFindingLogicStats());
    }

    @GetMapping(ApiRoutes.EP_STATS_DOWNLOADS_STATUS)
    public ResponseEntity<Map<ProcessStatus,Long>> countByStatus() {
        return ResponseEntity.ok(statsService.countByStatus());
    }


}
