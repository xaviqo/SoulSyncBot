package tech.xavi.soulsync.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.dto.gateway.GatewayToken;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdTokenDto;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyTokenDto;
import tech.xavi.soulsync.entity.ConfigurationField;
import tech.xavi.soulsync.repository.gateway.SlskdGateway;
import tech.xavi.soulsync.repository.gateway.SpotifyAuthGateway;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;

import java.time.Instant;
import java.util.Map;

@Log4j2
@Service @RequiredArgsConstructor
public class GatewayTokenService {

    private final SlskdGateway slskdGateway;
    private final SpotifyAuthGateway spotifyAuthGateway;
    private final ConfigurationFieldService configurationFieldService;
    private final Map<GatewayName,GatewayToken> gatewayTokens;

    public GatewayToken getToken(GatewayName gatewayName) {
        synchronized (gatewayName) {
            GatewayToken token = gatewayTokens.getOrDefault(
                    gatewayName,
                    GatewayToken.builder()
                            .gatewayName(gatewayName)
                            .build()
            );
            if (isTokenExpired(token)) {
                token = requestNewToken(gatewayName);
                gatewayTokens.put(gatewayName,token);
            }
            return token;
        }
    }

    public boolean isTokenPresent(GatewayToken gatewayToken){
        return (gatewayToken != null
                    && !gatewayToken
                    .token()
                    .isBlank());
    }

    private GatewayToken requestNewToken(GatewayName gatewayName){
        return switch (gatewayName) {
            case SLSKD -> getNewSlskdToken();
            case SPOTIFY -> getNewSpotifyToken();
        };
    }

    private GatewayToken getNewSpotifyToken(){
        SpotifyTokenDto spotifyToken = spotifyAuthGateway
                .getToken(getSpotifyB64credentials());
        return GatewayToken.builder()
                .token(spotifyToken.token())
                .expirationStamp(Instant.now().getEpochSecond() + spotifyToken.expires())
                .gatewayName(GatewayName.SPOTIFY)
                .build();
    }

    private GatewayToken getNewSlskdToken(){
        String slskdBaseUrl = configurationFieldService
                .getFieldWithValue(ConfigurationField.SLSKD_API_URL)
                .getValue()
                .asText();
        String username = configurationFieldService
                .getFieldWithValue(ConfigurationField.SLSKD_USERNAME)
                .getValue()
                .asText();
        String password = configurationFieldService
                .getFieldWithValue(ConfigurationField.SLSKD_PASSWORD)
                .getValue()
                .asText();
        SlskdTokenDto slskdToken = slskdGateway
                .baseUrl(slskdBaseUrl)
                .getToken(username,password);
        return GatewayToken.builder()
                .gatewayName(GatewayName.SLSKD)
                .token(slskdToken.token())
                .expirationStamp(slskdToken.expires())
                .build();
    }

    private boolean isTokenExpired(GatewayToken token){
        return Instant.now().getEpochSecond()
                > (token.expirationStamp() - 60);
    }

    private String getSpotifyB64credentials(){
        String spotifyClientId = configurationFieldService
                .getFieldWithValue(ConfigurationField.SPOTIFY_CLIENT_ID)
                .getValue()
                .asText();
        String spotifyApiSecret = configurationFieldService
                .getFieldWithValue(ConfigurationField.SPOTIFY_API_SECRET)
                .getValue()
                .asText();
        byte[] credentialBytes = (spotifyClientId+":"+spotifyApiSecret).getBytes();
        return java.util.Base64.getEncoder()
                .encodeToString(credentialBytes);
    }


}
