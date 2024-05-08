package tech.xavi.soulsync.service.download.process;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tech.xavi.soulsync.configuration.globals.ProcessStatus;
import tech.xavi.soulsync.entity.db.SlskdRequest;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class SearchProcess extends SlskdProcess {

    @Override
    public CompletableFuture<Void> execute(SlskdRequest request) {
        int min = 5000;
        int max = 60000;
        int randomNumber = new Random().nextInt(max - min + 1) + min;
        long rnd = Math.abs(randomNumber);
        log.trace(
                String.format("START - PROCESS [%s] - INPUT [%s] - RANDOM SEC [%s]",
                        getTaskName(),
                        request.getSearchInput(),
                        rnd
                )
        );
        try {
            Thread.sleep(rnd);
            log.trace(
                    String.format("FINISH - PROCESS [%s] - INPUT [%s] - RANDOM SEC [%s]",
                            getTaskName(),
                            request.getSearchInput(),
                            rnd
                    )
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return execute();
    }

    @Override
    public ProcessStatus getStatus()  {
        return ProcessStatus.SEARCHING;
    }

    @Override
    public String getTaskName() {
        return "SEARCH";
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
