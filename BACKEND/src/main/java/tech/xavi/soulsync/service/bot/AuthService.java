package tech.xavi.soulsync.service.bot;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdTokenRes;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyTokenRes;
import tech.xavi.soulsync.entity.SoulSyncConfiguration;
import tech.xavi.soulsync.entity.Token;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.service.configuration.ConfigurationService;

import java.time.Instant;

@Log4j2
@Service
public class AuthService {

    private final SpotifyGateway spotifyGateway;
    private final SlskdGateway slskdGateway;
    private Token spotifyToken;
    private Token slskdToken;

    public AuthService(
            SpotifyGateway spotifyGateway,
            SlskdGateway slskdGateway
    ) {
        this.spotifyGateway = spotifyGateway;
        this.slskdGateway = slskdGateway;
    }

    public synchronized Token getSpotifyToken(){
        if (spotifyToken == null || isTokenExpired(spotifyToken)){
            SpotifyTokenRes tokenResponse = spotifyGateway
                    .getToken(getB64credentials());
            long expirationStamp = Instant.now().getEpochSecond() + tokenResponse.getExpires();
            String tokenStr = tokenResponse.getToken();
            spotifyToken = Token.builder()
                    .apiName("Spotify")
                    .token(tokenStr)
                    .expirationStamp(expirationStamp)
                    .build();
        }
        return spotifyToken;
    }


    public synchronized Token getSlskdToken(){
        if (slskdToken == null || isTokenExpired(slskdToken)){
            SlskdTokenRes tokenResponse = slskdGateway
                    .getToken(
                            getConfiguration().getSlskdUsername(),
                            getConfiguration().getSlskdPassword()
                    );
            long expirationStamp = tokenResponse.getExpires();
            String tokenStr = tokenResponse.getToken();
            slskdToken = Token.builder()
                    .apiName("Slskd")
                    .token(tokenStr)
                    .expirationStamp(expirationStamp)
                    .build();
        }
        return slskdToken;
    }


    private String getB64credentials(){
        String credentialString = getConfiguration().getSpotifyClientId() + ":" + getConfiguration().getSpotifyClientSecret();
        byte[] credentialBytes = (credentialString).getBytes();
        return java.util.Base64.getEncoder()
                .encodeToString(credentialBytes);
    }

    private boolean isTokenExpired(Token token){
        long currentInSeconds = Instant.now().getEpochSecond();
        boolean isExpired = currentInSeconds > (token.expirationStamp() - 60);
        if (isExpired)
            log.debug("{} token is null or has expired. A new one is requested",token.apiName());
        return isExpired;
    }

    private SoulSyncConfiguration.Api getConfiguration(){
        return ConfigurationService.instance().cfg().api();
    }

}
