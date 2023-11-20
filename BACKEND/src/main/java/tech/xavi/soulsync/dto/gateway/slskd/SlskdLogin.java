package tech.xavi.soulsync.dto.gateway.slskd;

import lombok.Builder;

@Builder
public record SlskdLogin(String username, String password) {
}
