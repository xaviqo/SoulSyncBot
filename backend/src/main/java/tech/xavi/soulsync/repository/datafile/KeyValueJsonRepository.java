package tech.xavi.soulsync.repository.datafile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class KeyValueJsonRepository<K, V> {

    static final String ROUTE = "datafile/";
    @Getter final ObjectMapper objectMapper;
    final File file;
    Map<K, V> data;

    public KeyValueJsonRepository(ObjectMapper mapper, String jsonFile) {
        this.file = new File(ROUTE, jsonFile + ".json");
        this.objectMapper = mapper;
        this.data = new HashMap<>();
        loadData();
    }

    public void saveData() {
        try {
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(K key, V value) {
        data.put(key, value);
        saveData();
    }

    public V get(K key, Class<V> clazz){
        V value = data.get(key);
        if (value != null) {
            try {
                String json = objectMapper.writeValueAsString(value);
                return objectMapper.readValue(json, clazz);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Collection<V> getAll(){
        return this.data.values();
    }

    public boolean containsKey(K key) {
        return data.containsKey(key);
    }

    public void remove(K key) {
        data.remove(key);
        saveData();
    }

    public void clear() {
        data.clear();
        saveData();
    }

    private void loadData() {
        try {
            if (!file.exists()) {
                createDbDirectory();
                file.createNewFile();
                saveData();
            } else {
                data = objectMapper.readValue(file, getTypeReference());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract TypeReference<Map<K, V>> getTypeReference();

    private void createDbDirectory() {
        File directory = new File(ROUTE);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

}