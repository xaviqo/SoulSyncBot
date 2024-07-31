package tech.xavi.soulsync.dto.stats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Builder;

import java.util.Map;

@Builder
public record IterationStats(
        long banExpirationTime,
        long nextIteration,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        Map<Long,Integer> lastBans,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        Map<Long,Integer> lastIterations,
        int currentRequests,
        boolean isBanned,
        long minsUntilBanExpiry,
        float throttleMultiplier,
        long adjustedMillisBetweenRequests,
        long millisBetweenRequests
) {
}
