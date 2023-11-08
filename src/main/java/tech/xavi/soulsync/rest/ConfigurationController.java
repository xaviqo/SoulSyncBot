package tech.xavi.soulsync.rest;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.service.rest.ConfigurationRestService;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/configuration")
public class ConfigurationController {

    private final ConfigurationRestService cfgRestService;

    @GetMapping("/get")
    public ResponseEntity<List<ConfigurationField>> getConfigurationFields(@RequestParam String section){
        return ResponseEntity.ok(cfgRestService.getFieldsBySection(section));
    }

    @PostMapping("/save")
    public ResponseEntity<List<ConfigurationField>> saveConfiguration(
            @RequestParam String section,
            @RequestBody List<Map<String,Object>> configValues
    ){
        return ResponseEntity.ok(cfgRestService.saveConfiguration(section,configValues));
    }

    @PostMapping("/reset")
    public ResponseEntity<List<ConfigurationField>> resetSectionToDefault(@RequestParam String section){
        cfgRestService.resetSectionToDefault(section);
        return ResponseEntity.ok(cfgRestService.getFieldsBySection(section));
    }

}
