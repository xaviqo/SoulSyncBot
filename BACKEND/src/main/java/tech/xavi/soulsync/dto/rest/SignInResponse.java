package tech.xavi.soulsync.dto.rest;

import lombok.Builder;

@Builder
public record SignInResponse(
        String token,
        String user
) {
}
