package tech.xavi.soulsync.repository.file;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.repository.KeyValueJsonRepository;

import java.util.List;

@Repository @RequiredArgsConstructor
public class ConfigurationFieldRepository {

    private final KeyValueJsonRepository<String, JsonNode> keyValueRepo;

    public void save(List<ConfigurationField> fields){
        fields.forEach(this::save);
    }

    public synchronized JsonNode save(ConfigurationField field){
        synchronized (keyValueRepo) { keyValueRepo.put(field.name(), field.getValue()); }
        return field.getValue();
    }

    public synchronized JsonNode get(ConfigurationField field){
        return keyValueRepo.get(field.name(), JsonNode.class);
    }

    public synchronized boolean contains(ConfigurationField field){
        return keyValueRepo.containsKey(field.name());
    }


}
