package tech.xavi.soulsync.service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.configuration.globals.Role;
import tech.xavi.soulsync.configuration.globals.SearchInputStrategy;
import tech.xavi.soulsync.dto.account.AccountDto;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.entity.datafile.Account;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.service.integration.GatewayTokenService;
import tech.xavi.soulsync.service.search.SearchPolicyService;
import tech.xavi.soulsync.service.user.AccountService;

import java.util.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Log4j2
@RequiredArgsConstructor
@Service
public class InitialSetupService implements CommandLineRunner {

    private static final String[] DEFAULT_ADMIN_VALUES = {"admin","admin"};
    private final ConfigurationFieldService configurationFieldService;
    private final SearchPolicyService searchPolicyService;
    private final AccountService accountService;
    private final GatewayTokenService gatewayTokenService;
    private static final ConfigurationField[] INITIAL_SETUP_FIELDS = {
            ConfigurationField.SPOTIFY_CLIENT_ID,
            ConfigurationField.SPOTIFY_API_SECRET,
            ConfigurationField.SLSKD_USERNAME,
            ConfigurationField.SLSKD_PASSWORD,
            ConfigurationField.SLSKD_API_URL
    };
    private static final ConfigurationField.Section[] INITIAL_SETUP_SECTIONS = {
            ConfigurationField.Section.SEARCH,
            ConfigurationField.Section.MAINTENANCE
    };

    @Override
    public void run(String... args) throws Exception {
        createDefaultInstallation();
        createDefaultSearchPolicyConfiguration();
    }

    public void saveApiValues(List<ConfigurationFieldDto> setupFields){
        if (isAppInstalled())
            throw new SoulSyncException(
                    SoulSyncError.INIT_SETUP_ERROR_CFG,
                    HttpStatus.BAD_REQUEST
            );
        configurationFieldService
                .saveDtoFieldsWithoutCheckingValue(setupFields);
    }

    public void createAdminAccountAndFinish(AccountDto admin) {
        if (isAppInstalled())
            throw new SoulSyncException(
                    SoulSyncError.INIT_SETUP_ERROR_CFG,
                    HttpStatus.BAD_REQUEST
            );
        accountService.deleteAccount(DEFAULT_ADMIN_VALUES[0]);
        accountService.createAccount(
                Account.builder()
                        .username(admin.username())
                        .password(admin.password())
                        .build(),
                Role.ADMIN
        );
        configurationFieldService
                .saveFieldCheckingValue(ConfigurationField.IS_APP_INSTALLED,true);
    }

    public Map<String,Object> getInitialSetup(){
        if (!isAppInstalled())
            return Map.of(
                    "admin", getDefaultAccount(),
                    "fields", getInitialSetupFields()
            );
        return null;
    }

    public Map<String,Boolean> isAppInstalledResponse(){
        return Map.of("isInstalled",isAppInstalled());
    }

    public Map<GatewayName,Boolean> getApiChecks() {
        Map<GatewayName,Boolean> apiChecks = new HashMap<>();
        for (GatewayName gatewayName : GatewayName.values())
            apiChecks.put(gatewayName,testApiConnection(gatewayName));
        return apiChecks;
    }

    private List<ConfigurationField> getInitialSetupFields(){
        return Arrays.stream(INITIAL_SETUP_FIELDS)
                .map(configurationFieldService::getFieldWithValue)
                .toList();
    }

    private Account getDefaultAccount(){
        return Account.builder()
                .username(DEFAULT_ADMIN_VALUES[0])
                .password(DEFAULT_ADMIN_VALUES[1])
                .build();
    }

    private void createDefaultSearchPolicyConfiguration() {
        final String DEFAULT = "Default";

        if (searchPolicyService.containPolicyById(DEFAULT)) {
            log.info("Search policy configuration loaded");
        } else {
            SearchPolicy defaultSearchPolicy = SearchPolicy.builder()
                    .name(DEFAULT)
                    .id(DEFAULT)
                    .fileFormatsByComa(configurationFieldService.getProperty(ConfigurationField.SP_FILE_FORMATS))
                    .minCharsPerWord(configurationFieldService.getProperty(ConfigurationField.SP_MIN_CHARS_PER_WORD))
                    .wordsToRemoveByComa(configurationFieldService.getProperty(ConfigurationField.SP_WORDS_TO_REMOVE))
                    .minimumMp3Bitrate(configurationFieldService.getProperty(ConfigurationField.SP_MIN_MP3_BITRATE, Integer.class))
                    .minimumMinutesPerRetry(configurationFieldService.getProperty(ConfigurationField.SP_MINIMUM_MINS_RETRY))
                    .maxRetries(configurationFieldService.getProperty(ConfigurationField.SP_MAX_RETRIES))
                    .avoidRemix(configurationFieldService.getProperty(ConfigurationField.SP_IS_AVOID_REMIX))
                    .avoidLive(configurationFieldService.getProperty(ConfigurationField.SP_IS_AVOID_LIVE))
                    .inputStrategy(SearchInputStrategy.STANDARD_STRATEGY)
                    .downloadPriority(DownloadPriority.NORMAL)
                    .build();
            searchPolicyService.saveEntity(defaultSearchPolicy);
            log.warn("Search policy configuration not detected. Created default search policy with values from application.yml");
        }
    }

    private void createDefaultInstallation(){
        if (isAppInstalled()) {
            log.info("Minimum setup loaded successfully");
        } else {
            createDefaultUser();
            loadConfigurationBySection(INITIAL_SETUP_SECTIONS);
            log.warn("Minimum setup not detected");
            log.warn("Loaded initial configuration application with values from application.yml");
            log.warn("SPOTIFY AND/OR SLSKD APIs must be configured");
        }
    }

    private void loadConfigurationBySection(ConfigurationField.Section... sections) {
        configurationFieldService
                .getFieldsBySections(sections)
                .forEach( field ->
                        configurationFieldService
                                .saveFieldCheckingValue(
                                        field,
                                        configurationFieldService
                                                .getProperty(field)
                                )
                );
    }

    private void createDefaultUser(){
        try {
            accountService.createAccount(getDefaultAccount(),Role.ADMIN);
        } catch (Exception ignored) {}
        log.info("Default user created: {}/{}",DEFAULT_ADMIN_VALUES[0],DEFAULT_ADMIN_VALUES[1]);
    }

    private boolean isAppInstalled(){
        return configurationFieldService
                .getFieldWithValue(ConfigurationField.IS_APP_INSTALLED,false)
                .getValue()
                .asBoolean();
    }

    private boolean testApiConnection(GatewayName gatewayName){
        try {
            return gatewayTokenService
                    .isTokenPresent(gatewayTokenService.getToken(gatewayName));
        } catch (Exception e){
            return false;
        }
    }

}
