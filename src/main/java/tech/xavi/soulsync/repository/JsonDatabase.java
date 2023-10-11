package tech.xavi.soulsync.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class JsonDatabase<T> {

    private final String DATA_FIELD = "data";
    private final String LAST_UPATE_FIELD = "lastUpdate";
    private final Class<T> clazz;
    private final ObjectMapper objectMapper;
    private final File DB;
    private List<T> data;
    private long lastUpdate;

    public JsonDatabase(String jsonFile, Class<T> clazz) {
        this.clazz = clazz;
        this.data = new ArrayList<>();
        this.DB = new File("db/",jsonFile);
        this.objectMapper = new ObjectMapper();
        if (!createJsonFile()) loadData();
    }

    public List<T> get(){
        return this.data;
    }

    public void save(List<T> data) {
        this.data.addAll(data);
        this.lastUpdate = System.currentTimeMillis();

        Map<String,Object> json = new HashMap<>();
        json.put(LAST_UPATE_FIELD,this.lastUpdate);
        json.put(DATA_FIELD,data);

        try {
            objectMapper.writeValue(DB,json);
        } catch (IOException e) {
            log.error("Error saving data in {} at {}",
                    DB.getName(),
                    new Date(this.lastUpdate)
            );
        }
    }

    private void loadData(){
        log.info("File {} detected. Loading previous data",DB.getName());
        try {
            JsonNode jsonNode = objectMapper.readTree(DB);
            String dataArray = jsonNode.get(DATA_FIELD).toString();
            this.data = mapJsonToObjectList(dataArray).stream().map(obj -> (T)obj).toList();
            this.lastUpdate = jsonNode.get(LAST_UPATE_FIELD).intValue();
        } catch (Exception e) {
            log.error("Error loading data from {}",
                    DB.getName()
            );
            e.printStackTrace();
        }

    }

    private <T> List<T> mapJsonToObjectList(String json) throws Exception {
        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, clazz);
        return objectMapper.readValue(json,collectionType);
    }

    private boolean createJsonFile(){
        createDbDirectory();
        boolean exists = DB.exists();
        if (!exists) {
            try {
                if (DB.createNewFile())
                    log.info("New file has been created: {}",DB.getAbsoluteFile());
            } catch (IOException e) {
                log.error("Error creating JSON file: {}",DB.getAbsoluteFile());
            }
        } else {
            log.info("The file is already present: {}",DB.getAbsoluteFile());
        }
        return !exists;
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
