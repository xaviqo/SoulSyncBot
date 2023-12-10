package tech.xavi.soulsync.dto.service;

import lombok.Builder;

@Builder
public record AccountRequest(
        String username,
        String password
) {
}
