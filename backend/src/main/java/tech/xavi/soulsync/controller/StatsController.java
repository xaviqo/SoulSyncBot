package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.stats.IterationStats;
import tech.xavi.soulsync.service.throttle.SlskdRequestsThrottleService;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final SlskdRequestsThrottleService throttleService;

    @GetMapping(ApiRoutes.EP_STATS_ITERATIONS)
    public ResponseEntity<IterationStats> getIterationsStats() {
        return ResponseEntity.ok(throttleService.getIterationsStats());
    }
}
