package tech.xavi.soulsync.service.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.sub.ConfigurationField;
import tech.xavi.soulsync.entity.sub.SoulSyncError;
import tech.xavi.soulsync.configuration.security.SoulSyncException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CfgFieldsService {

    private final ObjectMapper mapper;

    public void checkValidFieldValue(ConfigurationField field) {
        JsonNode value = field.getValue();

        switch (field.getType()) {
            case TEXT:
                if (value.isTextual() && value.textValue().length() > 0) {return;}
                break;
            case NUMBER:
                if (value.isNumber()) {return;}
                break;
            case ARRAY:
                if (value.isArray() && value.size() > 0) {return;}
                break;
            case BOOLEAN:
                if (value.isBoolean()) {return;}
                break;
            case RANGE:
                if (isInRange(field)) {return;}
                break;
            case SELECT:
                if (isSelectOptionPresent(field)) {return;}
                break;
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
