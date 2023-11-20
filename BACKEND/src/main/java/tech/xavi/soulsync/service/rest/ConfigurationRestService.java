package tech.xavi.soulsync.service.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.service.auth.AccountService;
import tech.xavi.soulsync.service.configuration.CfgFieldsService;
import tech.xavi.soulsync.service.configuration.ConfigurationService;
import tech.xavi.soulsync.service.configuration.HealthService;

import java.util.*;

@Service
public class ConfigurationRestService {
    private final Map<ConfigurationField.Section,List<ConfigurationField>> CONFIGURATION_FIELDS;
    private final CfgFieldsService fieldsService;
    private final HealthService healthService;
    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    public ConfigurationRestService(
            CfgFieldsService fieldsService,
            HealthService healthService,
            AccountService accountService,
            ObjectMapper objectMapper
    ) {
        Map<ConfigurationField.Section,List<ConfigurationField>> fieldsBySection = new HashMap<>();
        for (ConfigurationField.Section section : ConfigurationField.Section.values()){
            fieldsBySection.put(section,getConfigurationFields(section));
        }
        this.CONFIGURATION_FIELDS = fieldsBySection;
        this.fieldsService = fieldsService;
        this.healthService = healthService;
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    public List<?> saveConfiguration(String sectionStr, List<Map<String,Object>> configValues) {
        boolean isUserSave =  (Objects.equals(sectionStr, "admin"));
        return isUserSave
                ? saveOrUpdateUser(configValues)
                : saveJsonConfiguration(sectionStr,configValues);
    }

    public List<ConfigurationField> saveJsonConfiguration(String sectionStr, List<Map<String,Object>> configValues){
        ConfigurationField.Section section = fieldsService.getSection(sectionStr);
        List<ConfigurationField> fields = fieldsService.fieldsMapToConfigurationFields(configValues);
        fields.forEach( field -> {
            fieldsService.checkValidFieldValue(field);
            getCfg().modifyConfigurationSubfield(
                    section,
                    field.getJsonFieldName(),
                    field.getValue()
            );
        });
        if (ConfigurationField.Section.API.equals(section)) {
            checkApisConfiguration(healthService.getHealthStatus());
        }
        getCfg().reloadConfiguration();
        return fields;
    }

    public void resetSectionToDefault(String sectionStr){
        ConfigurationField.Section section = fieldsService.getSection(sectionStr);
        getCfg().resetSectionDefaults(section);
        getCfg().reloadConfiguration();
    }

    public List<ConfigurationField> getFieldsBySection(String sectionStr){
        ConfigurationField.Section section = fieldsService.getSection(sectionStr);
        List<ConfigurationField> fields = new ArrayList<>();
        for (ConfigurationField field : CONFIGURATION_FIELDS.get(section)) {
            fields.add(getFieldCurrentValue(field));
        }
        return fields;
    }

    public void checkApisConfiguration(Map<String,Boolean> apisStatus){
        if (apisStatus.get("all-ok")) {
            getCfg().modifyConfigurationSubfield(
                    ConfigurationField.Section.API,
                    "configured",
                    objectMapper.valueToTree(true)
            );
        }
    }

    private List<ConfigurationField> getConfigurationFields(ConfigurationField.Section section){
        return Arrays
                .stream(ConfigurationField.values())
                .filter(field -> field.getSection().equals(section))
                .toList();
    }

    private ConfigurationField getFieldCurrentValue(ConfigurationField field){
        JsonNode currentValue = getCfg().readConfigurationNode(
                field.getSection(),field.getJsonFieldName()
        );
        field.setValue(currentValue);
        return field;
    }

    private List<?> saveOrUpdateUser(List<Map<String,Object>> configValues){
        String username = (String) configValues.get(0).get("value");
        String password = (String) configValues.get(1).get("value");
        boolean isNewAccount = configValues.get(2).get("value").equals("true");
        if (username != null && password != null) {
            if (isNewAccount) {
                accountService.createAccount(username,password);
            } else {
                accountService.updateAccount(username,password);
            }
        }
        return new ArrayList<>();
    }

    private ConfigurationService getCfg(){
        return ConfigurationService.instance();
    }
}

