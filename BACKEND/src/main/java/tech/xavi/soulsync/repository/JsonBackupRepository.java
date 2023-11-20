package tech.xavi.soulsync.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import tech.xavi.soulsync.entity.SoulSyncError;
import tech.xavi.soulsync.configuration.security.SoulSyncException;

import java.io.File;
import java.io.IOException;

@Log4j2
public class JsonBackupRepository<T> {

    private final Class<T> clazz;
    private final ObjectMapper objectMapper;
    private final File DB;
    private T data;

    public JsonBackupRepository(String jsonFile, Class<T> clazz) {
        this.clazz = clazz;
        this.data = null;
        this.DB = new File(
                "db/",
                jsonFile+".json"
        );
        this.objectMapper = new ObjectMapper();
        createJsonFile();
        loadData();
    }

    public void modifyField(String mainField, String subField, JsonNode newValue) {
        if (this.data == null) {
            return;
        }
        try {
            JsonNode jsonNode = objectMapper.valueToTree(this.data);
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                if (objectNode.has(mainField) && objectNode.get(mainField).isObject()) {
                    ObjectNode subObject = (ObjectNode) objectNode.get(mainField);
                    if (subObject.has(subField)){
                        subObject.set(subField,newValue);
                    }
                }
            }
            objectMapper.writeValue(this.DB,jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadData();
    }

    public T get(){
        return this.data;
    }

    public JsonNode getJsonAsNode(){
        try {
            return objectMapper.readTree(DB);
        } catch (IOException e) {
            throw new SoulSyncException(
                    SoulSyncError.ERROR_READING_JSON_CFG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public void save(T newData) {
        try {
            this.data = newData;
            objectMapper.writeValue(DB,newData);
        } catch (IOException e) {
            log.error("Error saving data in {}",
                    DB.getName()
            );
            e.printStackTrace();
        }
    }

    private void loadData(){
        log.info("Loading previous data from file {}",DB.getName());
        try {
            if (DB.length() > 0) {
                this.data = objectMapper.readValue(DB,clazz);
            } else {
                log.info("File {} is empty, no data to load",DB.getName());
            }
        } catch (Exception e) {
            log.error("Error loading data from {}", DB.getName());
            e.printStackTrace();
        }
    }

    private void createJsonFile(){
        createDbDirectory();
        if (!DB.exists()) {
            try {
                if (DB.createNewFile())
                    log.info("New file has been created: {}",DB.getAbsoluteFile());
            } catch (IOException e) {
                log.error("Error creating JSON file: {}",DB.getAbsoluteFile());
            }
        } else {
            log.info("The file is already present: {}",DB.getAbsoluteFile());
        }
    }

    private void createDbDirectory(){
        File directory = new File("db/");
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                log.info("New directory has been created: {}", directory.getAbsolutePath());
            } else {
                log.error("Error creating directory: {}", directory.getAbsolutePath());
            }
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
