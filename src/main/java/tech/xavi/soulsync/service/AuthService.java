package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.dto.gateway.SpotifyTokenResponse;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.model.Token;

@Log4j2
@Service
public class AuthService {

    private final String SPOTIFY_CLIENT_ID;
    private final String SPOTIFY_CLIENT_SECRET;
    private final SpotifyGateway spotifyGateway;
    private final SlskdGateway slskdGateway;
    private Token spotifyToken;
    private Token slskdToken;

    public AuthService(
            @Value("${tech.xavi.soulsync.credentials.spotify.client-id}") String spotClientId,
            @Value("${tech.xavi.soulsync.credentials.spotify.client-secret}") String spotClientSec,
            SpotifyGateway spotifyGateway,
            SlskdGateway slskdGateway
    ) {
        this.SPOTIFY_CLIENT_ID = spotClientId;
        this.SPOTIFY_CLIENT_SECRET = spotClientSec;
        this.spotifyGateway = spotifyGateway;
        this.slskdGateway = slskdGateway;
    }

    public Token getSpotifyToken(){
        if (spotifyToken == null || isTokenExpired(spotifyToken)){
            SpotifyTokenResponse tokenResponse = spotifyGateway
                    .getToken(getB64credentials());
            long expirationStamp = System.currentTimeMillis() + tokenResponse.getExpires();
            String tokenStr = tokenResponse.getToken();
            spotifyToken = Token.builder()
                    .token(tokenStr)
                    .expirationStamp(expirationStamp)
                    .build();
        }
        return spotifyToken;
    }



    private String getB64credentials(){
        byte[] credentialBytes = (SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).getBytes();
        return java.util.Base64.getEncoder()
                .encodeToString(credentialBytes);
    }

    private boolean isTokenExpired(Token token){
        long currentInSeconds = System.currentTimeMillis() / 1000;
        return currentInSeconds > (token.expirationStamp() - 60);
    }


}
