package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;

@Builder
public record SlskdLogin(String username, String password) {
}
