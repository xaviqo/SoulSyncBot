package tech.xavi.soulsync.service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.repository.JsonBackupRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Service
public class ConfigurationService {

    private final SoulSyncConfiguration configuration;
    private final SoulSyncConfiguration propertiesConfiguration;
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

    public synchronized SoulSyncConfiguration getConfiguration(){
        return this.configuration;
    }

    public void backupConfiguration(){
        jsonRepository.save(configuration);
    }

    private SoulSyncConfiguration loadConfigurationFromJSON(){
        SoulSyncConfiguration cfgFromJson = jsonRepository.get();
        if (cfgFromJson == null) {
            jsonRepository.save(propertiesConfiguration);
            return propertiesConfiguration;
        }
        return cfgFromJson;
    }

    private SoulSyncConfiguration loadConfigurationFromProperties(){
        return SoulSyncConfiguration.builder()
                .apiCfg(SoulSyncConfiguration.Api.builder()
                        .spotifyClientId(getProperty("api.spotify","client-id", String.class))
                        .spotifyClientSecret(getProperty("api.spotify","client-secret", String.class))
                        .slskdUsername(getProperty("api.slskd","username", String.class))
                        .slskdPassword(getProperty("api.slskd","password", String.class))
                        .slskdUrl(getProperty("api.slskd","url", String.class))
                        .build())
                .finderCfg(SoulSyncConfiguration.Finder.builder()
                        .wordsToRemove(getProperty("finder","common-words-to-remove",String.class).split(","))
                        .acceptedFormats(getProperty("finder","accepted-formats",String.class).split(","))
                        .minimumBitRate(getProperty("finder","min-mp3-bitrate",Integer.class))
                        .avoidRepeatFileWhenErrored(getProperty("finder","avoid-repeat-file-when-error",String.class).equals("true"))
                        .maxRetriesWaitingResult(getProperty("app","max-retries-wait-result",Integer.class))
                        .build())
                .appCfg(SoulSyncConfiguration.App.builder()
                        .pausesMs(getProperty("app","delay-ms", Integer.class))
                        .totalSimultaneousProcesses(getProperty("app","total-simultaneous-processes",Integer.class))
                        .waitingTimeBtwResultRequestsSec(getProperty("app","wait-sec-btw-result-req",Integer.class))
                        .intervalMinutesScheduledTask(getProperty("app","interval-minutes-scheduled-task",Integer.class))
                        .maxSongsDownloadingSameTime(getProperty("app","max-songs-downloading-same-time",Integer.class))
                        .build())
                .build();
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
