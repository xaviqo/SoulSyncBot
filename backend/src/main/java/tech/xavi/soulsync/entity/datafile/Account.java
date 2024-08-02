package tech.xavi.soulsync.entity.datafile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.xavi.soulsync.entity.Role;

import java.util.Collection;
import java.util.Collections;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Account implements UserDetails {

    private String username;
    private String password;
    private Role role;

    @Override @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton((GrantedAuthority) () -> role.name());
    }

    @Override @JsonIgnore
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override @JsonIgnore
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override @JsonIgnore
    public boolean isEnabled() {
        return false;
    }

}
