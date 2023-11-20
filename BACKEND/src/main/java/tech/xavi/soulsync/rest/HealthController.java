package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.constants.EndPoint;
import tech.xavi.soulsync.service.configuration.HealthService;
import tech.xavi.soulsync.service.rest.ConfigurationRestService;

import java.util.Map;

@RequiredArgsConstructor
@RestController
public class HealthController {

    private final HealthService healthService;
    private final ConfigurationRestService configurationRestService;

    @GetMapping(EndPoint.HEALTH)
    public ResponseEntity<Map<String,Boolean>> soulsyncHealthCheck(){
        Map<String,Boolean> apisStatus = healthService.getHealthStatus();
        configurationRestService.checkApisConfiguration(apisStatus);
        return ResponseEntity.ok(apisStatus);
    }

}
