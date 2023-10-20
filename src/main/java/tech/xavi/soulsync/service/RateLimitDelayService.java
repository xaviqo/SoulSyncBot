package tech.xavi.soulsync.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class RateLimitDelayService {

    private final int MS_DELAY;
    private final int SEC_SEARCH_PAUSE;
    private final AtomicInteger seeksRunning;

    public RateLimitDelayService(
            @Value("${tech.xavi.soulsync.cfg.delay-ms}") int msDelay,
            @Value("${tech.xavi.soulsync.cfg.wait-sec-btw-result-req}") int waitSecSearchPause
            ) {
        this.MS_DELAY = msDelay;
        this.SEC_SEARCH_PAUSE = waitSecSearchPause;
        this.seeksRunning = new AtomicInteger(0);
    }

    public int delay(){
        return delay(MS_DELAY);
    }

    public int delay(int ms){
        int totalProcesses = getCurrentProcesses();
        int delayMs = ms*totalProcesses;
        log.debug("[delay] - Total processes running: ({}) Time will be multiplied by: ({}) Delay applied: ({}ms)",
                totalProcesses,
                totalProcesses,
                delayMs
        );
        try {
            final int randomDelay = ThreadLocalRandom.current().nextInt(delayMs/2, delayMs*2);
            Thread.sleep(randomDelay);
            return randomDelay;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 0;
    }

    public int searchPause() throws InterruptedException{
        Thread.sleep(SEC_SEARCH_PAUSE * 1000L);
        return SEC_SEARCH_PAUSE;
    }

    private int getCurrentProcesses(){
        return seeksRunning.get();
    }

    public void initSeek(){
        int total = seeksRunning.incrementAndGet();
        log.debug("[initSeek] - New search process has started. Total processes running: {}",total);
    }

    public void finishSeek(){
        int total = seeksRunning.decrementAndGet();
        log.debug("[finishSeek] - A search process has been terminated. Total processes running: {}",total);
    }

}
