package example.api.service;

import example.api.model.Account;
import example.api.model.RolesTypes;
import example.api.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Data
@AllArgsConstructor
@Service
public class AccountService {

    private BCryptPasswordEncoder encoder;

    @Autowired
    private AccountRepository accountRepository;

    public Optional<Account> getAccountById(final Long id) { return accountRepository.findById(id); }

    public Iterable<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountByUsername (Account account) {
        return accountRepository.findByUsername(account.getUsername());
    }

    public void deleteAccountById (final Long id) {
        accountRepository.deleteById(id);
    }

    public Account saveAccount (Account account) {

        String encodedPassword = this.encoder.encode(account.getPassword());
        account.setPassword(encodedPassword);

        if(account.getUsername().contains("admin") || account.getUsername().contains("administrator")) {
            account.setRole(RolesTypes.ADMIN);
        } else {
            account.setRole(RolesTypes.USER);
        }

        account = this.accountRepository.save(account);

        return account;
    }

    public void deleteAllAccounts () {
        accountRepository.deleteAll();
    }
}
