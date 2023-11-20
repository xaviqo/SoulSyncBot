package tech.xavi.soulsync.service.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.entity.SoulSyncError;
import tech.xavi.soulsync.configuration.security.SoulSyncException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CfgFieldsService {

    private final ObjectMapper mapper;

    public void checkValidFieldValue(ConfigurationField field){
        JsonNode value = field.getValue();
        if (field.getType() == ConfigurationField.Type.TEXT) {
            if (value.isTextual()) return;
        } else if (field.getType() == ConfigurationField.Type.NUMBER) {
            if (value.isNumber()) return;
        } else if (field.getType() == ConfigurationField.Type.ARRAY) {
            if (value.isArray()) return;
        } else if (field.getType() == ConfigurationField.Type.BOOLEAN) {
            if (value.isBoolean()) return;
        } else if (field.getType() == ConfigurationField.Type.RANGE) {
            if (isInRange(field)) {
                return;
            }
        } else if (field.getType() == ConfigurationField.Type.SELECT) {
            if (isSelectOptionPresent(field)) {
                return;
            }
        }
        throw new SoulSyncException(
                SoulSyncError.FIELD_VALUE_NOT_VALID.buildMessage(
                        value.toString(),
                        field.getFieldName().toUpperCase(),
                        field.getType().getInputErrorMsg()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    private boolean isSelectOptionPresent(ConfigurationField field){
        for (Object option : Arrays.stream(field.getValues()).toArray())
            if (field.getValue().asText().equals(option.toString())) return true;
        return false;
    }

    private boolean isInRange(ConfigurationField configurationField){
        if (configurationField.getValue().isNumber()) {
            int value = configurationField.getValue().asInt();
            return (value >= configurationField.getMin()
                    && value <= configurationField.getMax());
        }
        return false;
    }

    public List<ConfigurationField> fieldsMapToConfigurationFields(List<Map<String,Object>> configValues){
        List<ConfigurationField> fields = new ArrayList<>();
        configValues.forEach( fieldMap -> {
            ConfigurationField cfgField = getCfgFieldByJsonField((String) fieldMap.get("jsonFieldName"));
            cfgField.setValue(mapper.valueToTree(fieldMap.get("value")));
            fields.add(cfgField);
        });
        return fields;
    }

    private ConfigurationField getCfgFieldByJsonField(String jsonFieldName){
        for (ConfigurationField field : ConfigurationField.values()){
            if (field.getJsonFieldName().equals(jsonFieldName)) return field;
        }
        throw new SoulSyncException(
                SoulSyncError.CFG_FIELD_NOT_FOUND.buildMessage(jsonFieldName),
                HttpStatus.BAD_REQUEST
        );
    }

    public ConfigurationField.Section getSection(String sectionStr) {
        for (ConfigurationField.Section section : ConfigurationField.Section.values()) {
            if (section.name().equalsIgnoreCase(sectionStr)) {
                return section;
            }
        }
        throw new SoulSyncException(
                SoulSyncError.CFG_SECTION_NOT_FOUND.buildMessage(sectionStr),
                HttpStatus.BAD_REQUEST
        );
    }

}
