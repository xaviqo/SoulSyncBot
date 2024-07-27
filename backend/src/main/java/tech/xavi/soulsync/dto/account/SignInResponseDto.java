package tech.xavi.soulsync.dto.account;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import tech.xavi.soulsync.entity.Role;
import tech.xavi.soulsync.dto.shared.AlertData;

@SuperBuilder @Getter
public class SignInResponseDto {
    private final String username;
    private final TokenDto token;
    private final Role role;
    private final AlertData alertData;
}
