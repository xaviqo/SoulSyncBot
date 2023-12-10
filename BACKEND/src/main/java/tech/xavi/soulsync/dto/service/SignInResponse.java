package tech.xavi.soulsync.dto.service;

import lombok.Builder;

@Builder
public record SignInResponse(
        String token,
        String user
) {
}
