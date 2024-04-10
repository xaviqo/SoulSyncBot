package tech.xavi.soulsync.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.service.configuration.InitialSetupService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class ConfigurationController {

    private final InitialSetupService initialSetupService;

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
