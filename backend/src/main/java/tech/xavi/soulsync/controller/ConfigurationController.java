package tech.xavi.soulsync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.globals.ApiRoutes;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.dto.account.AccountDto;
import tech.xavi.soulsync.dto.configuration.AppVersionDto;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.configuration.DemoModeService;
import tech.xavi.soulsync.service.configuration.SetupService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@RestController
public class ConfigurationController {

    private final SetupService setupService;
    private final ConfigurationFieldService configurationFieldService;
    private final DemoModeService demoModeService;

    @GetMapping(ApiRoutes.EP_FIELD)
    public ResponseEntity<ConfigurationField> getFieldByName(@RequestParam String name) {
        return ResponseEntity.ok(configurationFieldService.getFieldByName(name.toUpperCase()));
    }

    @PostMapping(ApiRoutes.EP_FIELDS)
    public ResponseEntity<Void> saveFieldsValue(@RequestBody List<ConfigurationFieldDto> fields){
        configurationFieldService.saveDtoFieldsCheckingValue(fields);
        return ResponseEntity.ok(null);
    }

    @GetMapping(ApiRoutes.EP_FIELDS)
    public ResponseEntity<Set<ConfigurationField>> getFieldsBySections(
            @RequestParam String sections,
            @RequestParam(required = false) boolean value
    ){
        return ResponseEntity.ok(configurationFieldService.getFieldsBySections(sections,value));
    }

    @GetMapping(ApiRoutes.EP_IS_INSTALLED)
    public ResponseEntity<Map<String,Boolean>> isAppInstalled(){
        return ResponseEntity.ok(setupService.isAppInstalledResponse());
    }

    @GetMapping(ApiRoutes.EP_INITIAL_SETUP)
    public ResponseEntity<Map<String,Object>> getInitialSetup(){
        return ResponseEntity.ok(setupService.getInitialSetup());
    }

    @PostMapping(ApiRoutes.EP_INIT_SETUP_APIS)
    public ResponseEntity<Map<GatewayName,Boolean>> saveApiValues(
            @RequestBody List<ConfigurationFieldDto> fields
    ){
        setupService.saveApiValues(fields);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(setupService.getApiChecks());
    }

    @PostMapping(ApiRoutes.EP_INIT_SETUP_ADMIN)
    public ResponseEntity<Void> setAdminAccountAndFinish(
            @RequestBody AccountDto adminAccount
            ){
        setupService
                .createAdminAccountAndFinish(adminAccount);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping(ApiRoutes.EP_APP_VERSION)
    public ResponseEntity<AppVersionDto> getAppVersion() {
        return ResponseEntity.ok(setupService.getCurrentAndLatestVersion());
    }

    @GetMapping(ApiRoutes.EP_IS_DEMO)
    public ResponseEntity<Boolean> isDemoMode() {
        return ResponseEntity.ok(demoModeService.isDemoMode());
    }
}
