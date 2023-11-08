package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.service.rest.HealthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/health")
public class HealthController {

    private final HealthService healthService;

    @GetMapping("/soulsync")
    public ResponseEntity<String> soulsyncHealthCheck(){
        return ResponseEntity.ok(healthService.soulSyncCheck());
    }

    @GetMapping("/slskd")
    public ResponseEntity<String> slskdHealthCheck(){
        return ResponseEntity.ok(healthService.slskdCheck());
    }
}
