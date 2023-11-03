package tech.xavi.soulsync.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class JsonBackupRepository<T> {

    private final String DATA_FIELD = "data";
    private final String LAST_UPATE_FIELD = "lastUpdate";
    private final Class<T> clazz;
    private final ObjectMapper objectMapper;
    private final File DB;
    private T data;
    private long lastUpdate;

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

    public T get(){
        return this.data;
    }

    public void save(T newData) {
        try {
            this.data = newData;
            objectMapper.writeValue(DB,newData);
        } catch (IOException e) {
            log.error("Error saving data in {} at {}",
                    DB.getName(),
                    new Date(this.lastUpdate)
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



}
