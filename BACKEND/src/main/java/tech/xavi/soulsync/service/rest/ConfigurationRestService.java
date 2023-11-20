package tech.xavi.soulsync.service.rest;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.service.configuration.CfgFieldsService;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.util.*;

@Service
public class ConfigurationRestService {
    private final Map<ConfigurationField.Section,List<ConfigurationField>> CONFIGURATION_FIELDS;
    private final CfgFieldsService fieldsService;

    public ConfigurationRestService(CfgFieldsService fieldsService) {
        Map<ConfigurationField.Section,List<ConfigurationField>> fieldsBySection = new HashMap<>();
        for (ConfigurationField.Section section : ConfigurationField.Section.values()){
            fieldsBySection.put(section,getConfigurationFields(section));
        }
        this.CONFIGURATION_FIELDS = fieldsBySection;
        this.fieldsService = fieldsService;
    }

    public List<ConfigurationField> saveConfiguration(String sectionStr, List<Map<String,Object>> configValues){
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
            JsonNode value = getCfg().readConfigurationNode(section,field.getJsonFieldName());
            field.setValue(value);
            fields.add(field);
        }
        return fields;
    }

    private List<ConfigurationField> getConfigurationFields(ConfigurationField.Section section){
        return Arrays
                .stream(ConfigurationField.values())
                .filter(field -> field.getSection().equals(section))
                .toList();
    }

    private ConfigurationService getCfg(){
        return ConfigurationService.instance();
    }
}
