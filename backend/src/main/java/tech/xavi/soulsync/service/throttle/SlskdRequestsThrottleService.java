package tech.xavi.soulsync.service.throttle;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tech.xavi.soulsync.configuration.globals.GatewayName;
import tech.xavi.soulsync.dto.stats.IterationStats;
import tech.xavi.soulsync.entity.FixedSizeMap;
import tech.xavi.soulsync.entity.datafile.ConfigurationField;
import tech.xavi.soulsync.entity.db.SlskdRequest;
import tech.xavi.soulsync.service.configuration.ConfigurationFieldService;
import tech.xavi.soulsync.service.integration.GatewayTokenService;
import tech.xavi.soulsync.service.integration.SlskdGatewayService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
@Getter
@Service
public class SlskdRequestsThrottleService {

    private static final int SEC_PER_ITERATION = 60;
    private final ConfigurationFieldService configurationFieldService;
    private final SlskdGatewayService slskdGatewayService;
    private final GatewayTokenService gatewayTokenService;
    private LocalDateTime banExpirationTime;
    private LocalDateTime nextIteration;
    private int currentRequests = 0;
    private final Map<Long,Integer> lastBans;
    private final Map<Long,Integer> lastIterations;

    public SlskdRequestsThrottleService(
            SlskdGatewayService slskdGatewayService,
            GatewayTokenService gatewayTokenService,
            ConfigurationFieldService configurationFieldService
    ) {
        banExpirationTime = null;
        nextIteration = LocalDateTime.now();
        lastIterations = new FixedSizeMap<>(60);
        lastBans = new FixedSizeMap<>(5);
        this.slskdGatewayService = slskdGatewayService;
        this.gatewayTokenService = gatewayTokenService;
        this.configurationFieldService = configurationFieldService;
    }

    public synchronized void throttle(SlskdRequest slskdRequest)  {
        if (isIterationExpired()) closeIteration();
        increaseIterationRequests();
        long msPause = getMillisBetweenRequests();
        log.trace("Pause Initiated... | SlskdReq: {} | MS: {} | Applied Multiplier: {}",slskdRequest,msPause,getThrottleMultiplier());
        try {
            TimeUnit.MILLISECONDS.sleep(msPause);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        log.trace("Pause Finished... | SlskdReq: {} ",slskdRequest);
    }

    public synchronized boolean isNotBanned() {
        LocalDateTime banExpTime = getBanExpirationTime();
        if (Objects.isNull(banExpTime)) return true;
        if (LocalDateTime.now().isAfter(banExpTime)) {
            log.warn("Ban period ended, proceed to reboot SLSKD...");
            banExpirationTime = null;
            slskdGatewayService.rebootSlskd();
            slskdGatewayService.waitForSlskdReboot();
            gatewayTokenService.requestNewToken(GatewayName.SLSKD);
            log.warn("SLSKD reboot finished, download queue continues");
            return true;
        }
        return false;
    }

    public synchronized void setBanned() {
        if (Objects.isNull(banExpirationTime)) {
            lastBans.put(
                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond(),
                    currentRequests
            );
            banExpirationTime = LocalDateTime
                    .now()
                    .plus(Duration.ofMinutes(30));
            currentRequests = 0;
        }
    }

    public IterationStats getIterationsStats() {
        boolean isBanned = Objects.nonNull(getBanExpirationTime());
        LocalDateTime banExpTime = Objects.nonNull(getBanExpirationTime())
                ? getBanExpirationTime()
                : LocalDateTime.now();
        return IterationStats.builder()
                .banExpirationTime(banExpTime.atZone(ZoneId.systemDefault()).toEpochSecond())
                .nextIteration(getNextIteration().atZone(ZoneId.systemDefault()).toEpochSecond())
                .lastBans(getLastBans())
                .lastIterations(getLastIterations())
                .currentRequests(getCurrentRequests())
                .isBanned(isBanned)
                .throttleMultiplier(getThrottleMultiplier())
                .adjustedMillisBetweenRequests(getMillisBetweenRequests())
                .millisBetweenRequests(configurationFieldService
                        .getValue(ConfigurationField.SRCH_MILLIS_BETWEEN_REQUESTS)
                        .asLong())
                .minsUntilBanExpiry(Duration.between(
                        LocalDateTime.now(),
                        banExpTime
                ).toMinutes())
                .build();
    }

    private synchronized boolean isIterationExpired() {
        return LocalDateTime
                .now()
                .isAfter(nextIteration);
    }

    private synchronized void closeIteration() {
        if (currentRequests > 0) {
            lastIterations.put(
                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond(),
                    currentRequests
            );
            currentRequests = 0;
        }
        nextIteration = LocalDateTime
                .now()
                .plus(Duration.ofMinutes(1));
    }

    public long getMillisBetweenRequests() {
        return (long) (configurationFieldService
                .getValue(ConfigurationField.SRCH_MILLIS_BETWEEN_REQUESTS)
                .asLong()
                * getThrottleMultiplier());
    }

    public float getThrottleMultiplier() {
        if (currentRequests < 10)
            return 1f;
        if (currentRequests < 15)
            return 1.2f;
        if (currentRequests < 20)
            return 1.4f;
        if (currentRequests < 25)
            return 1.6f;
        return 2f;
    }

    private void increaseIterationRequests() {
        currentRequests++;
    }

}
