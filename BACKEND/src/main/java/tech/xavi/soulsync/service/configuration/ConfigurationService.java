package tech.xavi.soulsync.service.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import tech.xavi.soulsync.entity.sub.ConfigurationField;
import tech.xavi.soulsync.entity.sub.RelocateOption;
import tech.xavi.soulsync.entity.sub.SoulSyncConfiguration;
import tech.xavi.soulsync.repository.JsonBackupRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConfigurationService {

    private static final AtomicReference<SoulSyncConfiguration> currentConfiguration = new AtomicReference<>(
            new SoulSyncConfiguration()
    );
    private static final JsonBackupRepository<SoulSyncConfiguration> jsonRepository = new JsonBackupRepository<>(
            "cfg",
            SoulSyncConfiguration.class
    );
    private final String CFG_FILE;


    private ConfigurationService(String cfgFile) {
        this.CFG_FILE = "/"+cfgFile;
        reloadConfiguration();
    }

    public static ConfigurationService instance(){
        return SoulSyncConfigurationServiceHolder.INSTANCE;
    }


    public SoulSyncConfiguration cfg(){
        return currentConfiguration.get();
    }

    public void resetSectionDefaults(ConfigurationField.Section section){
        JsonNode jsonNode = jsonRepository
                .getObjectMapper()
                .valueToTree(instance().getDefaultCfgBySection(section));
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


    public JsonNode readConfigurationNode(ConfigurationField.Section section, String field){
        return jsonRepository
                .getJsonAsNode()
                .get(section.getJsonCfgFieldName())
                .get(field);
    }

    public void reloadConfiguration(){
        currentConfiguration.set(
                loadConfigurationFromJSON(
                        loadConfigurationFromProperties()
                )
        );
    }

    private SoulSyncConfiguration loadConfigurationFromJSON(SoulSyncConfiguration propertiesConfiguration){
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
                        .minimumMinutesBtwSongCheck(getProperty("app","minimum-minutes-between-song-check",Integer.class))
                        .build();
            }
            case FINDER -> {
                return SoulSyncConfiguration.Finder.builder()
                        .wordsToRemove(getProperty("finder","common-words-to-remove",String.class).split(","))
                        .acceptedFormats(getProperty("finder","accepted-formats",String.class).split(","))
                        .minimumBitRate(getProperty("finder","min-mp3-bitrate",Integer.class))
                        .avoidRepeatFileWhenErrored(getProperty("finder","avoid-repeat-file-when-error",String.class).equals("true"))
                        .maxRetriesWaitingResult(getProperty("finder","max-retries-wait-result",Integer.class))
                        .avoidLive(getProperty("finder","avoid-live",String.class).equals("true"))
                        .avoidRemix(getProperty("finder","avoid-remix",String.class).equals("true"))
                        .build();
            }
        }
        return null;
    }

    private <T> T getProperty(String group, String prop, Class<T> targetType) {
        String value = getPropertiesReader().getProperty(getFullPropertyName(group, prop));
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

    private Properties getPropertiesReader(){
        try {
            Properties properties = new Properties();
            InputStream inputStream = ConfigurationService.class
                    .getResourceAsStream(this.CFG_FILE);
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SoulSyncConfigurationServiceHolder {
        private static final ConfigurationService INSTANCE
                = new ConfigurationService("soulsync-cfg.properties");
    }

}
