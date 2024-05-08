package tech.xavi.soulsync.service.integration;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class RateLimiterService {

    private long cooldownMs = 1800000;
    private long minInterval = 1000;
    private long lastRequestTime;
    private boolean isInCooldown = false;

    public synchronized CompletableFuture<Void> doPause() throws InterruptedException {
        lastRequestTime = getNextRequestTime();
        Thread.sleep(lastRequestTime);
        return CompletableFuture.completedFuture(null);
    }

    private long getNextRequestTime(){
        return Math.abs((lastRequestTime - System.currentTimeMillis()) + minInterval);
    }

    public boolean isInCooldown(){
        return isInCooldown;
    }

    public void activateCooldown(boolean inCooldown){
        lastRequestTime = System.currentTimeMillis() + cooldownMs;
    }

}
