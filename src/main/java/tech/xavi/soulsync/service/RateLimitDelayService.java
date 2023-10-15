package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class RateLimitDelayService {

    private final int MIN_MS_DELAY;
    private final int MAX_MS_DELAY;
    private final int LARGE_PAUSE_TRIGGER;
    private final int LARGE_PAUSE_MS;
    private final AtomicInteger pauseCounter;
    private final AtomicInteger seeksRunning;

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
        this.pauseCounter = new AtomicInteger(0);
        this.seeksRunning = new AtomicInteger(0);
    }

    public synchronized int delay(){
        return delay(MIN_MS_DELAY,MAX_MS_DELAY);
    }

    public int delay(int minMs, int maxMs){
        int totalProcesses = getCurrentProcesses();
        int minimum = minMs*totalProcesses;
        int maximum = maxMs*totalProcesses;
        if (totalProcesses > 1) {
            log.debug("Processes running at the same time ({}) The base pause time will be multiplied by ({})",
                    totalProcesses,
                    totalProcesses
            );
        }
        try {
            final int randomDelay = ThreadLocalRandom.current().nextInt(minimum, maximum);
            Thread.sleep(randomDelay);
            checkLargePauseRequired();
            return randomDelay;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 0;
    }

    private synchronized void checkLargePauseRequired(){
        if (pauseCounter.incrementAndGet() >= LARGE_PAUSE_TRIGGER){
            int totalProcesses = getCurrentProcesses();
            int largePause = LARGE_PAUSE_MS*totalProcesses;
            pauseCounter.set(0);
            log.debug("[searchSongs] - Total pLarge Pause Required. Milliseconds: {}",largePause);
            delay(largePause-1,largePause+1);
            log.debug("[searchSongs] - Large Pause Completed. Milliseconds: {}",largePause);
        }
    }

    private int getCurrentProcesses(){
        return seeksRunning.get();
    }

    public synchronized void initSeek(){
        int total = seeksRunning.incrementAndGet();
        log.debug("[initSeek] - New search process has started. Total: {}",total);

    }

    public synchronized void finishSeek(){
        int total = seeksRunning.decrementAndGet();
        log.debug("[finishSeek] - A search process has been terminated. Total: {}",total);
    }

}
