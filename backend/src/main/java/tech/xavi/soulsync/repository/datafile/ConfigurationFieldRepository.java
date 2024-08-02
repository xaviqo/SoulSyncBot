package tech.xavi.soulsync.repository.datafile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;

import java.util.List;
import java.util.Map;

@Repository
public class ConfigurationFieldRepository extends KeyValueJsonRepository<String,JsonNode>{

    public ConfigurationFieldRepository(ObjectMapper mapper) {
        super(mapper, "configuration");
    }

    public void save(List<ConfigurationField> fields){
        fields.forEach(this::save);
    }

    public synchronized JsonNode save(ConfigurationField field){
        synchronized (this.data) { put(field.name(), field.getValue()); }
        return field.getValue();
    }

    public synchronized JsonNode get(ConfigurationField field){
        return this.get(field.name(), JsonNode.class);
    }

    public synchronized boolean contains(ConfigurationField field){
        return this.containsKey(field.name());
    }

    @Override
    protected TypeReference<Map<String, JsonNode>> getTypeReference() {
        return new TypeReference<Map<String, JsonNode>>() {};
    }

}
