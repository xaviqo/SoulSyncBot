package tech.xavi.soulsync.dto.gateway;

import lombok.Builder;
import tech.xavi.soulsync.configuration.globals.GatewayName;

@Builder
public record GatewayToken(String token, long expirationStamp, GatewayName gatewayName) {
}

