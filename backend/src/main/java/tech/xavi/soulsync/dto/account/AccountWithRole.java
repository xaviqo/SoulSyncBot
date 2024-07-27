package tech.xavi.soulsync.dto.account;

import lombok.Builder;
import tech.xavi.soulsync.entity.Role;

@Builder
public record AccountWithRole(
        String username,
        Role role
) {
}
