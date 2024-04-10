package tech.xavi.soulsync.service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.shared.ConfigurationFieldDto;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.configuration.globals.Role;
import tech.xavi.soulsync.entity.Account;
import tech.xavi.soulsync.exception.SoulSyncError;
import tech.xavi.soulsync.exception.SoulSyncException;
import tech.xavi.soulsync.service.user.AccountService;
import tech.xavi.soulsync.service.integration.GatewayTokenService;

import java.util.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class InitialSetupService implements CommandLineRunner {

    private final ConfigurationFieldService configurationFieldService;
    private final AccountService accountService;
    private final GatewayTokenService gatewayTokenService;
    private final ConfigurationField[] INITIAL_SETUP_FIELDS = {
            ConfigurationField.SPOTIFY_CLIENT_ID,
            ConfigurationField.SPOTIFY_API_SECRET,
            ConfigurationField.SLSKD_USERNAME,
            ConfigurationField.SLSKD_PASSWORD,
            ConfigurationField.SLSKD_API_URL
    };

    @Override
    public void run(String... args) throws Exception {
        if (isAppInstalled()) {
            log.info("Minimum configuration detected in database.");
        } else {
            createDefaultUser();
            log.warn("Minimum configuration not detected in database. " +
                    "SPOTIFY AND/OR SLSKD must be configured");
        }
    }

    public Map<GatewayName, Boolean> setAndCheckInitialSetupValues(List<ConfigurationFieldDto> setupFields){
        if (isAppInstalled() || Objects.requireNonNull(setupFields).size() != INITIAL_SETUP_FIELDS.length)
            throw new SoulSyncException(
                    SoulSyncError.INIT_SETUP_ERROR_CFG,
                    HttpStatus.BAD_REQUEST
            );
        List<ConfigurationField> configurationFields = configurationFieldService.mapToConfigurationField(setupFields);
        configurationFieldService.saveFields(configurationFields);
        Map<GatewayName,Boolean> apiChecks = new HashMap<>();
        for (GatewayName gatewayName : GatewayName.values()){
            apiChecks.put(gatewayName,testApiConnection(gatewayName));
        }
        setInstalled(apiChecks);
        return apiChecks;
    }

    public List<ConfigurationField> getInitialSetupFields(){
        return Arrays.stream(this.INITIAL_SETUP_FIELDS)
                .map(configurationFieldService::getFieldWithValue)
                .toList();
    }

    public Map<String,Boolean> isAppInstalledResponse(){
        return Map.of("isInstalled",isAppInstalled());
    }

    private void setInstalled(Map<GatewayName,Boolean> apiChecks) {
        boolean allTrue = apiChecks
                .values()
                .stream()
                .allMatch(val -> val.equals(true));
        if (allTrue){
            configurationFieldService.
                    saveField(ConfigurationField.IS_APP_INSTALLED,true);
        }
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

    private void createDefaultUser(){
        final String user = "admin";
        final String pass = "admin";
        try {
            accountService.createAccount(
                    Account.builder()
                            .username(user)
                            .password(pass)
                            .build(),
                    Role.USER
            );
        } catch (SoulSyncException ignore){
        }
        log.info("Default user created: {}/{}",user,pass);
    }

}
