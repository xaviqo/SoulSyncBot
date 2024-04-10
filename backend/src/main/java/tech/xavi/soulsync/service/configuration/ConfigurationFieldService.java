package tech.xavi.soulsync.service.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.repository.file.ConfigurationFieldRepository;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class ConfigurationFieldService {

    private final ConfigurationFieldRepository configurationFieldRepository;
    private final ObjectMapper mapper;

    public ConfigurationFieldService(
            ConfigurationFieldRepository configurationFieldRepository,
            ObjectMapper mapper
    ) {
        this.configurationFieldRepository = configurationFieldRepository;
        this.mapper = mapper;
    }

    public void saveFields(List<ConfigurationField> fields){
        fields.forEach(this::checkAndSave);
    }

    public ConfigurationField saveField(ConfigurationField field, Object value){
        field.setValue(mapper.valueToTree(value));
        checkAndSave(field);
        return field;
    }

    public List<ConfigurationField> getFromFields(ConfigurationField... fields){
        return Arrays.stream(fields)
                .map(this::getFieldWithValue)
                .toList();
    }

    public ConfigurationField getFieldWithValue(ConfigurationField field, Object valueIfNotPresent){
        if (configurationFieldRepository.contains(field)) {
            field.setValue(configurationFieldRepository.get(field));
            return field;
        } else {
            return saveField(field,valueIfNotPresent);
        }
    }

    public ConfigurationField getFieldWithValue(ConfigurationField field){
        field.setValue(configurationFieldRepository.get(field));
        return field;
    }

    public List<ConfigurationField> mapToConfigurationField(List<ConfigurationFieldDto> fieldDtos){
        return fieldDtos.stream()
                .map(this::mapToConfigurationField)
                .toList();
    }

    public ConfigurationField mapToConfigurationField(ConfigurationFieldDto fieldDto){
        ConfigurationField field = ConfigurationField
                .findEnumByName(fieldDto.name());
        field.setValue(mapper.convertValue(fieldDto.value(), JsonNode.class));
        return field;
    }

    private void checkAndSave(ConfigurationField field){
        JsonNode value = field.getValue();
        try {
            switch (field.getDataType()) {
                case TEXT:
                    if (value.isTextual() && value.textValue().length() > 0) {break;}
                case NUMBER:
                    if (value.isNumber()) {break;}
                case ARRAY:
                    if (value.isArray() && value.size() > 0) {break;}
                    break;
                case BOOLEAN:
                    if (value.isBoolean()) {break;}
                case RANGE:
                    if (isInRange(field)) {break;}
                case SELECT:
                    if (isSelectOptionPresent(field)) {break;}
            }
            configurationFieldRepository.save(field);
        } catch (Exception exception){
            SoulSyncException soulSyncException = new SoulSyncException(
                    SoulSyncError.INVALID_VALUE,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    new Object[]{
                            field.getValue(),
                            field.getName(),
                            field.getDataType()
                    }
            );
            log.error(soulSyncException.getUserMessage(),exception);
            throw soulSyncException;
        }
    }

    private boolean isInRange(ConfigurationField field) throws Exception {
        if (field.getValue().isNumber()) {
            long value = field.getValue().longValue();
            return (value >= field.getMin()
                    && value <= field.getMax());
        }
        return false;
    }


    private boolean isSelectOptionPresent(ConfigurationField field){
        for (Object option : (Object[]) field.getDefaultValues())
            if (field.getValue().equals(option)) return true;
        return false;
    }


}