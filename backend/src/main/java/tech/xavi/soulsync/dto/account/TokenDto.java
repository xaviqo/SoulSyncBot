package tech.xavi.soulsync.dto.account;

import lombok.Builder;

import java.util.Date;

@Builder
public record TokenDto(
        String token,
        Date expirationDate
) {
}
