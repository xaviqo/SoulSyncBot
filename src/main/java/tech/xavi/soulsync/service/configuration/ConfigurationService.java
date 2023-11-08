package tech.xavi.soulsync.service.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.entity.RelocateOption;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.repository.JsonBackupRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
@Service
public class ConfigurationService {

    private SoulSyncConfiguration configuration;
    private SoulSyncConfiguration propertiesConfiguration;
    private final JsonBackupRepository<SoulSyncConfiguration> jsonRepository;
    private final Properties properties;

    public ConfigurationService(@Value("${tech.xavi.soulsync.cfg-file}") String cfgFile) {
        this.jsonRepository = new JsonBackupRepository<>(
                "soulsync_cfg",
                SoulSyncConfiguration.class
        );
        try {
            properties = new Properties();
            InputStream inputStream = ConfigurationService.class
                    .getResourceAsStream("/"+cfgFile);
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.propertiesConfiguration = loadConfigurationFromProperties();
        this.configuration = loadConfigurationFromJSON();
    }

    public void modifyConfigurationSubfield(
            ConfigurationField.Section section,
            String subField,
            JsonNode newValue
            ) {
        jsonRepository.modifyField(
                section.getJsonCfgFieldName(),
                subField,
                newValue
        );
    }

    public synchronized SoulSyncConfiguration getConfiguration(){
        return this.configuration;
    }

    public JsonNode readConfigurationNode(ConfigurationField.Section section, String field){
        return jsonRepository
                .getJsonAsNode()
                .get(section.getJsonCfgFieldName())
                .get(field);
    }

    public void reloadConfiguration(){
        this.configuration = loadConfigurationFromJSON();
    }

    public void resetSectionDefaults(ConfigurationField.Section section){
        JsonNode jsonNode = jsonRepository
                .getObjectMapper()
                .valueToTree(getDefaultCfgBySection(section));
        var fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            modifyConfigurationSubfield(
                    section,
                    field.getKey(),
                    field.getValue()
            );
        }
    }

    private SoulSyncConfiguration loadConfigurationFromJSON(){
        SoulSyncConfiguration cfgFromJson = jsonRepository.get();
        if (cfgFromJson == null) {
            jsonRepository.save(propertiesConfiguration);
            log.info("JSON configuration file not found. Loaded default values from properties");
            return propertiesConfiguration;
        }
        return cfgFromJson;
    }

    private SoulSyncConfiguration loadConfigurationFromProperties(){
        return SoulSyncConfiguration.builder()
                .apiCfg((SoulSyncConfiguration.Api) getDefaultCfgBySection(ConfigurationField.Section.API))
                .finderCfg((SoulSyncConfiguration.Finder) getDefaultCfgBySection(ConfigurationField.Section.FINDER))
                .appCfg((SoulSyncConfiguration.App) getDefaultCfgBySection(ConfigurationField.Section.BOT))
                .build();
    }

    private Object getDefaultCfgBySection(ConfigurationField.Section section){
        switch (section){
            case API -> {
                return SoulSyncConfiguration.Api.builder()
                        .spotifyClientId(getProperty("api.spotify","client-id", String.class))
                        .spotifyClientSecret(getProperty("api.spotify","client-secret", String.class))
                        .slskdUsername(getProperty("api.slskd","username", String.class))
                        .slskdPassword(getProperty("api.slskd","password", String.class))
                        .slskdUrl(getProperty("api.slskd","url", String.class))
                        .build();
            }
            case BOT -> {
                return SoulSyncConfiguration.App.builder()
                        .pausesMs(getProperty("app","delay-ms", Integer.class))
                        .totalSimultaneousProcesses(getProperty("app","total-simultaneous-processes",Integer.class))
                        .intervalMinutesScheduledTask(getProperty("app","interval-minutes-scheduled-task",Integer.class))
                        .maxSongsDownloadingSameTime(getProperty("app","max-songs-downloading-same-time",Integer.class))
                        .slskdDownloadsRoute(getProperty("app","slskd-downloads-route",String.class))
                        .userFilesRoute(getProperty("app","user-files-route",String.class))
                        .relocateFiles(getProperty("app","relocate-files",String.class).equals("true"))
                        .moveOrCopyFiles(RelocateOption.getOption(getProperty("app","move-files-or-copy",String.class)))
                        .renameCopiedFiles(getProperty("app","rename-copied-files",String.class).equals("true"))
                        .build();
            }
            case FINDER -> {
                return SoulSyncConfiguration.Finder.builder()
                        .wordsToRemove(getProperty("finder","common-words-to-remove",String.class).split(","))
                        .acceptedFormats(getProperty("finder","accepted-formats",String.class).split(","))
                        .minimumBitRate(getProperty("finder","min-mp3-bitrate",Integer.class))
                        .avoidRepeatFileWhenErrored(getProperty("finder","avoid-repeat-file-when-error",String.class).equals("true"))
                        .maxRetriesWaitingResult(getProperty("app","max-retries-wait-result",Integer.class))
                        .build();
            }
        }
        return null;
    }

    private <T> T getProperty(String group, String prop, Class<T> targetType) {
        String value = properties.getProperty(getFullPropertyName(group, prop));
        if (value != null) {
            if (targetType.isAssignableFrom(String.class)) {
                return targetType.cast(value);
            } else if (targetType.isAssignableFrom(Integer.class)) {
                try {
                    return targetType.cast(Integer.parseInt(value));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    private String getFullPropertyName(String group, String prop){
        final String SOULSYNC_PACKAGE = "tech.xavi.soulsync.cfg.";
        return SOULSYNC_PACKAGE+group+"."+prop;
    }

}
