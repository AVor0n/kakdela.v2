package ru.hh.kakdela.v2.security;

import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

  @Getter
  UUID id;
  @Getter
  private final int tokenVersion;

  public CustomUserDetails(
      UUID id,
      String username,
      @Nullable String password,
      int tokenVersion,
      Collection<? extends GrantedAuthority> authorities
  ) {
    super(username, password, authorities);
    this.id = id;
    this.tokenVersion = tokenVersion;
  }

  public CustomUserDetails(
      UUID id,
      String username,
      @Nullable String password,
      int tokenVersion,
      boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired,
      boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities
  ) {
    super(username,
        password,
        enabled,
        accountNonExpired,
        credentialsNonExpired,
        accountNonLocked,
        authorities);
    this.id = id;
    this.tokenVersion = tokenVersion;
  }
}
