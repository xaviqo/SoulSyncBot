package tech.xavi.soulsync.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;

import java.time.LocalDateTime;

@RestController
public class HealthController {

    @GetMapping(ApiRoutes.EP_HEALTH_CHECK)
    public ResponseEntity<LocalDateTime> getApiHealth(){
        return ResponseEntity.ok(LocalDateTime.now());
    }



}
