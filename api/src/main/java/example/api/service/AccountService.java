package example.api.service;

import example.api.controller.AccountController;
import example.api.model.Account;
import example.api.model.Role;
import example.api.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
@Service
public class AccountService implements UserDetailsService {

    private static final Logger log = Logger.getLogger(AccountController.class.getName());

    @Autowired
    private AccountRepository accountRepository;

    public Optional<Account> getAccountById(final Long id) { return accountRepository.findById(id); }

    public Iterable<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountByUsername (String username) throws AccountNotFoundException {
        Account retrievedAccount = accountRepository.findByUsername(username);

        if(retrievedAccount == null) {
            throw new AccountNotFoundException("Account not find");
        } else {
            return retrievedAccount;
        }
    }

    public void deleteAccountById (final Long id) {
        accountRepository.deleteById(id);
    }

    public Account saveAccount (Account account) {
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(
                10,
                new SecureRandom());

        String encodedPassword = bCrypt.encode(account.getPassword());
        account.setPassword(encodedPassword);

        if(account.getUsername().contains("admin") || account.getUsername().contains("administrator")) {
            account.setRoles(Collections.singleton(Role.ADMIN));
        } else {
            account.setRoles(Collections.singleton(Role.USER));
        }

        account = this.accountRepository.save(account);

        log.info(account.getUsername());
        log.info(account.getPassword());
        log.info(account.getEmail());
        log.info(account.getAuthorities().toString());
        return account;
    }

    public void deleteAllAccounts () {
        accountRepository.deleteAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Account account = accountRepository.findByUsername(username);

        if(account == null) {
            throw new UsernameNotFoundException("Unable to find account with the username : " + username);
        }

//        return org.springframework.security.core.userdetails.User
//                .withUsername(account.getUsername())
//                .password(account.getPassword())
//                .roles(String.valueOf(account.getRoles()))
//                .build();

        return new User(
                account.getUsername(),
                account.getPassword(),
                account.getAuthorities()
        );
    }
}
