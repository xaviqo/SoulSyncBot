package tech.xavi.soulsync.service.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.slskd.SlskdTokenRes;
import tech.xavi.soulsync.dto.gateway.spotify.SpotifyTokenRes;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.entity.Token;

@Log4j2
@Service
public class AuthService {

    private final String SPOTIFY_CLIENT_ID;
    private final String SPOTIFY_CLIENT_SECRET;
    private final String SLSKD_USER;
    private final String SLSKD_PASS;
    private final SpotifyGateway spotifyGateway;
    private final SlskdGateway slskdGateway;
    private Token spotifyToken;
    private Token slskdToken;

    public AuthService(
            @Value("${tech.xavi.soulsync.credentials.spotify.client-id}") String spotClientId,
            @Value("${tech.xavi.soulsync.credentials.spotify.client-secret}") String spotClientSec,
            @Value("${tech.xavi.soulsync.credentials.slskd.username}") String slskdUsr,
            @Value("${tech.xavi.soulsync.credentials.slskd.password}") String slskdPwd,
            SpotifyGateway spotifyGateway,
            SlskdGateway slskdGateway
    ) {
        this.SPOTIFY_CLIENT_ID = spotClientId;
        this.SPOTIFY_CLIENT_SECRET = spotClientSec;
        this.SLSKD_USER = slskdUsr;
        this.SLSKD_PASS = slskdPwd;
        this.spotifyGateway = spotifyGateway;
        this.slskdGateway = slskdGateway;
    }

    public synchronized Token getSpotifyToken(){
        if (spotifyToken == null || isTokenExpired(spotifyToken)){
            SpotifyTokenRes tokenResponse = spotifyGateway
                    .getToken(getB64credentials());
            long expirationStamp = System.currentTimeMillis() + tokenResponse.getExpires();
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
                    .getToken(SLSKD_USER,SLSKD_PASS);
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
        byte[] credentialBytes = (SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).getBytes();
        return java.util.Base64.getEncoder()
                .encodeToString(credentialBytes);
    }

    private boolean isTokenExpired(Token token){
        long currentInSeconds = System.currentTimeMillis() / 1000;
        boolean isExpired = currentInSeconds > (token.expirationStamp() - 60);
        if (isExpired)
            log.debug("{} token is null or has expired. A new one is requested",token.apiName());
        return isExpired;
    }

}
