package tech.xavi.soulsync.service.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.entity.sub.SoulSyncConfiguration;
import tech.xavi.soulsync.service.config.ConfigurationService;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class PauseService {

    private final AtomicInteger seeksRunning;

    public PauseService() {
        this.seeksRunning = new AtomicInteger(0);
    }

    public int delay(){
        return delay(getConfiguration().getPausesMs());
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
        Thread.sleep(1000);
        return 1;
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

    private SoulSyncConfiguration.App getConfiguration(){
        return ConfigurationService.instance().cfg().app();
    }

}
