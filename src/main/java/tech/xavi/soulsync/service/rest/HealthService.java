package tech.xavi.soulsync.service.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.gateway.SlskdGateway;

@Log4j2
@RequiredArgsConstructor
@Service
public class HealthService {

    private final SlskdGateway slskdGateway;

    public String slskdCheck(){
        return slskdGateway.isSlskdApiHealthy()
                ? "OK" : "KO";
    }

    public String soulSyncCheck(){
        return "OK";
    }


    public void initLogCheck(){
        if (slskdGateway.isSlskdApiHealthy())
            log.debug("[healthCheck] - Slsk API connection ----> SUCCESS");
        else
            log.error("[healthCheck] - Slsk API connection ----> FAIL");
    }
}
