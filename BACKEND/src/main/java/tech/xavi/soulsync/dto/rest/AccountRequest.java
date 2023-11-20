package tech.xavi.soulsync.dto.rest;

import lombok.Builder;

@Builder
public record AccountRequest(
        String username,
        String password
) {
}
