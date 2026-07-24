package ru.hh.kakdela.v2.security;

import java.util.HashSet;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.hh.kakdela.v2.dao.AccountDao;
import ru.hh.kakdela.v2.model.Account;

@RequiredArgsConstructor
@Service(value = "userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

  private final AccountDao accountDao;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account = accountDao.findByLogin(username)
        .orElseThrow(() -> new UsernameNotFoundException("Аккаунт не найден: login=" + username));
    if (account.getIsDeleted() != null && account.getIsDeleted()) {
      throw new UsernameNotFoundException("Аккаунт удалён: login=" + username);
    }
    return new CustomUserDetails(
        account.getId(), account.getLogin(), account.getPasswordHash(), new HashSet<>());
  }

  public UserDetails toUserDetails(Account account) {
        return new CustomUserDetails(
            account.getId(),
            account.getLogin(),
            account.getPasswordHash(),
            new HashSet<>()
        );
    }
}
