package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Log4j2
@Service
public class RateLimitDelayService {

    private final int MIN_MS_DELAY;
    private final int MAX_MS_DELAY;
    private final int LARGE_PAUSE_TRIGGER;
    private final int LARGE_PAUSE_MS;
    private int pauseCounter;

    public RateLimitDelayService(
            @Value("${tech.xavi.soulsync.cfg.delay-min-ms}") int minMsDelay,
            @Value("${tech.xavi.soulsync.cfg.delay-max-ms}") int maxMsDelay,
            @Value("${tech.xavi.soulsync.cfg.delay-large-pause-trigger}") int pauseTrig,
            @Value("${tech.xavi.soulsync.cfg.delay-large-pause-ms}") int largePauseMs
    ) {
        this.MIN_MS_DELAY = minMsDelay;
        this.MAX_MS_DELAY = maxMsDelay;
        this.LARGE_PAUSE_MS = largePauseMs;
        this.LARGE_PAUSE_TRIGGER = pauseTrig;
        this.pauseCounter = 0;
    }

    public int delay(){
        return delay(MIN_MS_DELAY,MAX_MS_DELAY);
    }

    public int delay(int minMs, int maxMs){
        try {
            final int randomDelay = ThreadLocalRandom.current().nextInt(minMs, maxMs);
            Thread.sleep(randomDelay);
            checkLargePauseRequired();
            return randomDelay;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 0;
    }

    private void checkLargePauseRequired(){
        pauseCounter++;
        if (pauseCounter >= LARGE_PAUSE_TRIGGER){
            pauseCounter = 0;
            log.debug("[searchSongs] - Large Pause Required. Milliseconds: {}",LARGE_PAUSE_MS);
            delay(LARGE_PAUSE_MS-1,LARGE_PAUSE_MS+1);
            log.debug("[searchSongs] - Large Pause Completed. Milliseconds: {}",LARGE_PAUSE_MS);
        }
    }

}
