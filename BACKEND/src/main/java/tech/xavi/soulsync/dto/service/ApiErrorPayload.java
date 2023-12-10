package tech.xavi.soulsync.dto.service;

import lombok.Builder;

@Builder
public record ApiErrorPayload(
        String message,
        String error,
        int code,
        long moment
) {
}
