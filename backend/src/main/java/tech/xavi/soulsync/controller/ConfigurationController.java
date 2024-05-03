package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.configuration.InitialSetupService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController
public class ConfigurationController {

    private final InitialSetupService initialSetupService;
    private final ConfigurationFieldService configurationFieldService;

    @GetMapping(ApiRoutes.EP_FIELDS)
    public ResponseEntity<Set<ConfigurationField>> getFieldsBySections(@RequestParam String sections){
        return ResponseEntity.ok(configurationFieldService.getFieldsBySections(sections));
    }

    @GetMapping(ApiRoutes.EP_IS_INSTALLED)
    public ResponseEntity<Map<String,Boolean>> isAppInstalled(){
        return ResponseEntity.ok(initialSetupService.isAppInstalledResponse());
    }

    @GetMapping(ApiRoutes.EP_INITIAL_SETUP)
    public ResponseEntity<List<ConfigurationField>> getInitialSetupValues(){
        return ResponseEntity.ok(initialSetupService.getInitialSetupFields());
    }

    @PostMapping(ApiRoutes.EP_INITIAL_SETUP)
    public ResponseEntity<Map<GatewayName,Boolean>> setInitialSetupValues(
            @RequestBody List<ConfigurationFieldDto> initialSetupValues
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(initialSetupService
                        .setAndCheckInitialSetupValues(initialSetupValues));
    }
}
