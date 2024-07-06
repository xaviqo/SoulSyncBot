package tech.xavi.soulsync.service.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.repository.datafile.ConfigurationFieldRepository;
import tech.xavi.soulsync.repository.property.ConfigurationPropertyRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@RequiredArgsConstructor
@Service
public class ConfigurationFieldService {

    private final ConfigurationPropertyRepository configurationPropertyRepository;
    private final ConfigurationFieldRepository configurationFieldRepository;
    private final ObjectMapper mapper;

    public ConfigurationField getFieldByName(String name) {
        for (ConfigurationField field : ConfigurationField.values())
            if (field.getName().equals(name)) return getFieldWithValue(field);
        return null;
    }

    public void saveDtoFieldsWithoutCheckingValue(List<ConfigurationFieldDto> fieldDtos) {
        mapToConfigurationField(fieldDtos)
                .forEach(configurationFieldRepository::save);
    }

    public void saveDtoFieldsCheckingValue(List<ConfigurationFieldDto> fieldDtos) {
        List<ConfigurationField> fields = mapToConfigurationField(fieldDtos);
        saveFieldsCheckingValue(fields);
    }

    public void saveFieldsCheckingValue(List<ConfigurationField> fields){
        fields.forEach(this::checkAndSave);
    }

    public <T> T getProperty(ConfigurationField cfgField, Class<T> clazz) {
        return getProperty(
                configurationPropertyRepository.getProperty(cfgField),
                clazz
        );
    }

    public <T> T getProperty(ConfigurationField cfgField) {
        return (T) getProperty(
                configurationPropertyRepository.getProperty(cfgField),
                cfgField.getClazz()
        );
    }

    public <T> T getProperty(Object value, Class<T> clazz) {
        return (T) clazz.cast(value);
    }

    public ConfigurationField saveFieldCheckingValue(ConfigurationField field, Object value){
        field.setValue(mapper.valueToTree(value));
        checkAndSave(field);
        return field;
    }

    public Set<ConfigurationField> getFieldsBySections(String sectionsByComa, boolean addValues) {
        Set<ConfigurationField> fields = Arrays.stream(sectionsByComa.split(","))
                .flatMap(section -> ConfigurationField.getAllBySection(section).stream())
                .collect(Collectors.toSet());
        if (addValues)
            return getValueFromFields(fields);
        else
            return fields;
    }

    public Set<ConfigurationField> getFieldsBySections(ConfigurationField.Section... section) {
        return Stream.of(section)
                .flatMap( s -> ConfigurationField.getAllBySection(s.name()).stream() )
                .collect(Collectors.toSet());
    }

    private Set<ConfigurationField> getValueFromFields(Set<ConfigurationField> fields){
        return fields.stream()
                .map(this::getFieldWithValue)
                .collect(Collectors.toSet());
    }

    public JsonNode getValue(ConfigurationField cfgField) {
        return getFieldWithValue(cfgField).getValue();
    }

    public ConfigurationField getFieldWithValue(ConfigurationField field, Object valueIfNotPresent){
        if (configurationFieldRepository.contains(field)) {
            field.setValue(configurationFieldRepository.get(field));
            return field;
        } else {
            return saveFieldCheckingValue(field,valueIfNotPresent);
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
        switch (field.getDataType()) {
            case TEXT:
                if (!value.isTextual() || value.textValue().length() < 0)
                    throw createInvalidValueException(field, "Text value is required");
                break;
            case NUMBER:
                if (!isValidNumber(field))
                    throw createInvalidValueException(
                            field,
                            String.format("Invalid number. Max %s, min %s.",
                                    field.getMax(),
                                    field.getMin()
                            )
                    );
                break;
            case ARRAY:
                if (!value.isArray() && value.size() < 1)
                    throw createInvalidValueException(field, "Non-empty array required");
                break;
            case BOOLEAN:
                if (!value.isBoolean())
                    throw createInvalidValueException(field, "Boolean value required");
                break;
            case RANGE:
                if (!isInRange(field))
                    throw createInvalidValueException(
                            field,
                            String.format("Value not in range. Max %s, min %s.",
                                    field.getMax(),
                                    field.getMin()
                            )
                    );
                break;
            case SELECT:
                if (!isSelectOptionPresent(field))
                    throw createInvalidValueException(field, "Select option not present");
                break;
            default:
                SoulSyncException soulSyncException = new SoulSyncException(
                        SoulSyncError.INVALID_TYPE,
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
                log.error(soulSyncException.getUserMessage());
                throw soulSyncException;
        }
        configurationFieldRepository.save(field);
    }

    private boolean isValidNumber(ConfigurationField field){
        JsonNode value = field.getValue();
        return value.isNumber()
                && value.asLong() >= field.getMin()
                && value.asLong() <= field.getMax();
    }

    private boolean isInRange(ConfigurationField field) {
        if (field.getValue().isNumber()) {
            long value = field.getValue().longValue();
            return (value >= field.getMin()
                    && value <= field.getMax());
        }
        return false;
    }


    private boolean isSelectOptionPresent(ConfigurationField field){
        for (Object option : (Object[]) field.getDefaultValues())
            if (String
                    .valueOf(field.getValue())
                    .replace("\"", "")
                    .equals(String.valueOf(option))
            ) return true;
        return false;
    }

    private SoulSyncException createInvalidValueException(ConfigurationField field, String message){
        return new SoulSyncException(
                SoulSyncError.INVALID_VALUE,
                HttpStatus.BAD_REQUEST,
                new Object[]{
                        field.name(),
                        message
                }
        );
    }


}