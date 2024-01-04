package tech.xavi.soulsync.rest;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.configuration.constants.EndPoint;
import tech.xavi.soulsync.entity.sub.ConfigurationField;
import tech.xavi.soulsync.service.auth.AuthService;
import tech.xavi.soulsync.service.rest.ConfigurationRestService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class ConfigurationController {

    private final ConfigurationRestService cfgRestService;
    private final AuthService authService;

    @GetMapping(EndPoint.CFG_GET_CONFIGURATION_FIELDS)
    public ResponseEntity<List<ConfigurationField>> getConfigurationFields(@RequestParam String section){
        return ResponseEntity.ok(cfgRestService.getFieldsBySection(section));
    }

    @PostMapping(EndPoint.CFG_SAVE_SECTION_CONFIGURATION)
    public ResponseEntity<List<?>> saveSectionConfiguration(
            @RequestParam String section,
            @RequestBody List<Map<String,Object>> configValues
    ){
        return ResponseEntity.ok(cfgRestService.saveConfiguration(section,configValues));
    }

    @PostMapping(EndPoint.CFG_RESET_SECTION)
    public ResponseEntity<List<ConfigurationField>> resetSectionToDefault(@RequestParam String section){
        cfgRestService.resetSectionToDefault(section);
        return ResponseEntity.ok(cfgRestService.getFieldsBySection(section));
    }

    @PostMapping(EndPoint.CFG_RENEW_TKN)
    public ResponseEntity<?> renewTokens(){
        authService.renewTokens();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(EndPoint.CFG_REBOOT_BOT)
    public ResponseEntity<?> reboot(){
        cfgRestService.rebootBot();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
