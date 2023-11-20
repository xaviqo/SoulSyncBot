package tech.xavi.soulsync.service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.gateway.SlskdGateway;
import tech.xavi.soulsync.gateway.SpotifyGateway;
import tech.xavi.soulsync.service.auth.AuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
@RequiredArgsConstructor
@Service
public class HealthService {

    private final SpotifyGateway spotifyGateway;
    private final SlskdGateway slskdGateway;
    private final AuthService authService;

    public Map<String,Boolean> getHealthStatus(){
        Map<String,Boolean> apisHealth = new HashMap<>();
        boolean isSpotifyOk = spotifyGateway.isSpotifyApiHealthy();
        boolean isSlskdOk = slskdGateway.isSlskdApiHealthy();
        apisHealth.put("spotify-response",isSpotifyOk);
        apisHealth.put("slskd-response",isSlskdOk);
        apisHealth.put("spotify-cfg-ok", isSpotifyOk && Objects.nonNull(authService.getSpotifyToken()));
        apisHealth.put("slskd-cfg-ok",isSlskdOk && Objects.nonNull(authService.getSlskdToken()));
        apisHealth.put("all-ok",allOk(apisHealth));
        return apisHealth;
    }

    private boolean allOk(Map<String,Boolean> apisHealth){
        boolean allTrue = true;
        for (Boolean status : apisHealth.values()) {
            if (!status) {
                allTrue = false;
                break;
            }
        }
        return allTrue;
    }

    public Map<String,Boolean> initLogCheck(){
        Map<String,Boolean> apisStatus = getHealthStatus();
        log.info(
                "[healthCheck] - SLSKD API CONNECTION CHECK ----> {}",
                apisStatus.get("spotify-response") ? "SUCCESS" : "FAILED"
        );
        log.info(
                "[healthCheck] - SLSKD API CREDENTIALS CHECK ----> {}",
                apisStatus.get("spotify-cfg-ok") ? "SUCCESS" : "FAILED"
        );
        log.info(
                "[healthCheck] - SPOTIFY API CONNECTION CHECK ----> {}",
                apisStatus.get("slskd-response") ? "SUCCESS" : "FAILED"
        );
        log.info(
                "[healthCheck] - SPOTIFY API CREDENTIALS CHECK ----> {}",
                apisStatus.get("slskd-cfg-ok") ? "SUCCESS" : "FAILED"
        );
        return apisStatus;
    }
}
