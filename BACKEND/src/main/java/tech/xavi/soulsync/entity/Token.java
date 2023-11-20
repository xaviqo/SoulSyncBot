package tech.xavi.soulsync.entity;

import lombok.Builder;

@Builder
public record Token(String token, long expirationStamp, String apiName) {
}
