package ru.hh.kakdela_v2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.hh.kakdela_v2.dao.AccountDao;
import ru.hh.kakdela_v2.model.Account;

import java.util.HashSet;

@Service(value = "userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

  private final AccountDao accountDao;

  @Autowired
  public UserDetailsServiceImpl(AccountDao accountDao) {
    this.accountDao = accountDao;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account = accountDao.findByLogin(username)
        .orElseThrow(() -> new UsernameNotFoundException("Аккаунт не найден: login=" + username));
    return new org.springframework.security.core.userdetails.User(account.getLogin(), account.getPasswordHash(), new HashSet<>());
  }

}
