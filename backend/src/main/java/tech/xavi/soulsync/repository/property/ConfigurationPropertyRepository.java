package tech.xavi.soulsync.repository.property;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;

import java.util.Map;

@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "tech.xavi.soulsync")
@PropertySource("classpath:application.yml")
public class ConfigurationPropertyRepository {

    private Map<String, Object> cfg;

    public Object getProperty(ConfigurationField cfgField) {
        String propertyKey = cfgField
                .name()
                .toLowerCase()
                .replace("_","-");
        return getProperty(propertyKey);
    }

    public Object getProperty(String key) {
        return getProperty(cfg, key, "");
    }

    private Object getProperty(Map<String, ?> properties, String desiredProperty, String prefix) {
        if (properties == null) {
            return null;
        }
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            String currentProperty = prefix + entry.getKey();
            if (entry.getValue() instanceof Map) {
                Object result = getProperty((Map<String, Object>) entry.getValue(), desiredProperty, currentProperty + ".");
                if (result != null) return result;
            } else if (entry.getKey().equals(desiredProperty)) {
                return entry.getValue();
            }
        }
        return null;
    }

}

