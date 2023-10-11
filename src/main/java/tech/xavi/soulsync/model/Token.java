package tech.xavi.soulsync.model;

import lombok.Builder;

@Builder
public record Token(String token, long expirationStamp) {
}
