package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.dto.rest.ResponseWrapper;
import tech.xavi.soulsync.service.rest.HealthService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/health")
public class HealthController {

    private final HealthService healthService;

    @GetMapping("/soulsync")
    public ResponseEntity<ResponseWrapper<String>> soulsyncHealthCheck(){
        return new ResponseEntity<>(
                ResponseWrapper.wrapResponse(
                        healthService.soulSyncCheck(),
                        String.class
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/slskd")
    public ResponseEntity<ResponseWrapper<String>> slskdHealthCheck(){
        return new ResponseEntity<>(
                ResponseWrapper.wrapResponse(
                        healthService.slskdCheck(),
                        String.class
                ),
                HttpStatus.OK
        );
    }
}
