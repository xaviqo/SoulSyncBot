package tech.xavi.soulsync.service.configuration;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.Banner;
import tech.xavi.soulsync.configuration.globals.DownloadPriority;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.configuration.globals.SearchInputStrategy;
import tech.xavi.soulsync.dto.account.AccountDto;
import tech.xavi.soulsync.dto.configuration.AppVersionDto;
import tech.xavi.soulsync.dto.gateway.github.LastReleaseDto;
import tech.xavi.soulsync.dto.shared.AlertData;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.dto.shared.MessageSeverity;
import tech.xavi.soulsync.entity.Role;
import tech.xavi.soulsync.entity.datafile.Account;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.datafile.SearchPolicy;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.repository.gateway.GithubGateway;
import tech.xavi.soulsync.service.integration.GatewayTokenService;
import tech.xavi.soulsync.service.search.SearchPolicyService;
import tech.xavi.soulsync.service.user.AccountService;

import java.util.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Log4j2
@Service
public class SetupService implements CommandLineRunner {

    private static final String[] DEFAULT_ADMIN_VALUES = {"admin","admin"};
    private static final ConfigurationField.Section[] INITIAL_SETUP_SECTIONS = {
            ConfigurationField.Section.SEARCH,
            ConfigurationField.Section.MAINTENANCE
    };

    private final String CURRENT_VERSION;

    private final ConfigurationFieldService configurationFieldService;
    private final SearchPolicyService searchPolicyService;
    private final AccountService accountService;
    private final GatewayTokenService gatewayTokenService;
    private final GithubGateway githubGateway;

    public SetupService(
            @Value("${tech.xavi.soulsync.version}") String currVer,
            ConfigurationFieldService configurationFieldService,
            SearchPolicyService searchPolicyService,
            AccountService accountService,
            GatewayTokenService gatewayTokenService,
            GithubGateway githubGateway
    ) {
        this.CURRENT_VERSION = currVer;
        this.configurationFieldService = configurationFieldService;
        this.searchPolicyService = searchPolicyService;
        this.accountService = accountService;
        this.gatewayTokenService = gatewayTokenService;
        this.githubGateway = githubGateway;
    }

    @Override
    public void run(String... args) throws Exception {
        createDefaultInstallation();
        createDefaultSearchPolicyConfiguration();

        AppVersionDto appVersion = getCurrentAndLatestVersion();

        printInitBanner();
        if (appVersion.getAlertData().getSeverity().equals(MessageSeverity.INFO))
            log.info(appVersion.getAlertData().getMessage());
        else
            log.warn(appVersion.getAlertData().getMessage());
        if (isDemoMode()) log.info(" ---> DEMO MODE ACTIVE <---");
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
                        .username(admin.getUsername())
                        .password(admin.getPassword())
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

    public AppVersionDto getCurrentAndLatestVersion() {
        LastReleaseDto releaseDto = githubGateway.getLastRelease();
        AppVersionDto appVersionDto = AppVersionDto.builder()
                .current(CURRENT_VERSION)
                .latest(releaseDto.tagName())
                .build();
        if (Objects.nonNull(releaseDto.message()) && releaseDto.message().equals(HttpStatus.NOT_FOUND.name())) {
            AlertData alertData = AlertData.builder()
                    .severity(MessageSeverity.ERROR)
                    .message(String.format("Not able to obtain data of the latest version of the application - Current: %s",
                            CURRENT_VERSION))
                    .build();
            appVersionDto
                    .setAlertData(alertData);
        } else if (!releaseDto.tagName().equals(CURRENT_VERSION)) {
            AlertData alertData = AlertData.builder()
                    .severity(MessageSeverity.INFO)
                    .message(String.format("A new version of the application is available! Latest: %s - Current: %s",
                            releaseDto.tagName(),
                            CURRENT_VERSION))
                    .build();
            appVersionDto
                    .setAlertData(alertData);
        } else {
            AlertData alertData = AlertData.builder()
                    .severity(MessageSeverity.SUCCESS)
                    .message(String.format("The latest version of the application is being used. - Current: %s",
                            CURRENT_VERSION))
                    .build();
            appVersionDto
                    .setAlertData(alertData);
        }
        return appVersionDto;
    }

    public boolean isDemoMode() {
        return configurationFieldService
                .getFieldWithValue(ConfigurationField.IS_DEMO_MODE,false)
                .getValue()
                .asText()
                .equalsIgnoreCase("true");
    }

    private List<ConfigurationField> getInitialSetupFields(){
        return Arrays.stream(new ConfigurationField[]{
                        ConfigurationField.SPOTIFY_CLIENT_ID,
                        ConfigurationField.SPOTIFY_API_SECRET,
                        ConfigurationField.SLSKD_USERNAME,
                        ConfigurationField.SLSKD_PASSWORD,
                        ConfigurationField.SLSKD_API_URL
                })
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
                    .avoidRadioEdit(configurationFieldService.getProperty(ConfigurationField.SP_IS_AVOID_RADIO_EDIT))
                    .avoidMixedTrack(configurationFieldService.getProperty(ConfigurationField.SP_IS_AVOID_MIXED_TRACK))
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
            createDefaultUsers();
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

    private void createDefaultUsers(){
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

    private void printInitBanner() {
        final int[] versionNumbers = getVersionNumbers();
        for (int i = 0; i < Banner.SPACE.getLines().length; i++)
            System.out.println(
                    Banner.SPACE.getLine(i)
                    + Banner.SOULSYNC.getLine(i)
                    + Banner.SPACE.getLine(i)
                    + Banner.V.getLine(i)
                    + Banner.values()[versionNumbers[0]].getLine(i)
                    + Banner.DOT.getLine(i)
                    + Banner.values()[versionNumbers[1]].getLine(i)
                    + Banner.DOT.getLine(i)
                    + Banner.values()[versionNumbers[2]].getLine(i)
            );
        System.out.println();
    }

    private int[] getVersionNumbers() {
        String numericPart = CURRENT_VERSION.split("-")[0];
        String[] parts = numericPart.split("\\.");
        int[] versionNumbers = new int[parts.length];

        for (int i = 0; i < parts.length; i++)
            versionNumbers[i] = Integer.parseInt(parts[i]);
        return versionNumbers;
    }

}
