package ru.hh.kakdela_v2.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.model.Account;

import java.util.HashSet;

@RequiredArgsConstructor
@Service(value = "userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

  private final AccountDao accountDao;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account = accountDao.findByLogin(username)
        .orElseThrow(() -> new UsernameNotFoundException("Аккаунт не найден: login=" + username));
    return new CustomUserDetails(account.getId(), account.getLogin(), account.getPasswordHash(), new HashSet<>());
  }

}
