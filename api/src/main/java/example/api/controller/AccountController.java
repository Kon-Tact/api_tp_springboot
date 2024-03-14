package example.api.controller;

import example.api.model.Account;
import example.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.net.Authenticator;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*", methods = {
        RequestMethod.OPTIONS,
        RequestMethod.DELETE,
        RequestMethod.POST,
        RequestMethod.GET,
        RequestMethod.HEAD,
        RequestMethod.PATCH,
        RequestMethod.PUT,
        RequestMethod.TRACE
})
@RequiredArgsConstructor
public class AccountController {

    public enum Status {
        SUCCESS,
        ERROR
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    private SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    private static final Logger log = Logger.getLogger(AccountController.class.getName());

    @Autowired
    private AccountService accountService;

    public String consoleFormat(Status status, String message) {

        String successColor = "\u001B[32m";
        String errorColor = "\u001B[31m";
        String resetColor = "\u001B[0m";
        String finalMessage = "";

        return switch (status) {
            case SUCCESS -> finalMessage = successColor + message + resetColor;
            case ERROR -> finalMessage = errorColor + message + resetColor;
        };
    }

    private HttpHeaders createCORSHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Headers", "Content-Type, Origin, X-Requested-With, Accept, Content, Authorization");
        return headers;
    }

    @RequestMapping(value = "/account/login", method = {RequestMethod.OPTIONS})
    public ResponseEntity<Account> handleOptionsSaveAccount() {
        log.info("Option de POST appelée");
        try {
            HttpHeaders headers = createCORSHeaders();
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/account/delete", method = {RequestMethod.OPTIONS})
    public ResponseEntity<Account> handleOptionsDeleteAccount() {
        try {
            HttpHeaders headers = createCORSHeaders();
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : Admin
    @GetMapping("/account/list")
    public Iterable<Account> getAllAccount() {
        Iterable<Account> accountList = null;
        try {
            accountList = accountService.getAllAccounts();
            log.info(consoleFormat(Status.SUCCESS,
                    "[Get Account List - DONE]  -- Status : " + HttpStatus.OK));
            return accountList;
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Get Account List - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : User
    @GetMapping("/account/{id}")
    public Optional<Account> getAccountById(final Long id) {

        Optional<Account> account = Optional.empty();
        try {
            account = accountService.getAccountById(id);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Get Account - DONE]  -- Status : " + HttpStatus.OK));
            return account;
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Get Account - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : Admin
    @DeleteMapping("/account/delete/{id}")
    public ResponseEntity<String> deleteAccountById(@RequestParam final Long id) {
        try {
            accountService.deleteAccountById(id);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Delete Account - DONE]  -- Status : " + HttpStatus.OK));
            String successMessage = "{\"Status\": \"200\", \"Méthode\": \"Delete account\"}";
            return new ResponseEntity<>(successMessage, createCORSHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Base clear - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            String errorMessage = "{\"Status\": \"Error\", \"Méthode\": \"Delete account\"}";
            return new ResponseEntity<>(errorMessage, createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : Admin
    @DeleteMapping("/account/clear")
    public ResponseEntity<String> deleteAllAccounts() {
        try {
            accountService.deleteAllAccounts();
            log.info(consoleFormat(Status.SUCCESS,
                    "[Delete all accounts - DONE]  -- Status : " + HttpStatus.OK));
            String successMessage = "{\"Status\": \"200\", \"Méthode\": \"Delete all accounts\"}";
            return new ResponseEntity<>(successMessage, createCORSHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Delete all accounts - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            String errorMessage = "{\"Status\": \"Error\", \"Méthode\": \"Delete all account\"}";
            return new ResponseEntity<>(errorMessage, createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : Tout le monde
    @PostMapping("/account/save")
    public ResponseEntity<Account> saveAccount(@RequestBody Account account) {
        log.info("Méthode save Account appelée");

        try {
            accountService.saveAccount(account);
            log.info(consoleFormat(Status.SUCCESS, "[Save Account - DONE]  -- Status : " + HttpStatus.OK));
            return new ResponseEntity<>(account, createCORSHeaders(), HttpStatus.OK);
        } catch (Exception e) {

            log.severe("Exception during saveAccount: " + e.getMessage());
            return new ResponseEntity<>(account, createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : User
    @PutMapping("/account/edit")
    public ResponseEntity<Account> editAccount(@RequestBody Account account) {
        int strength = 10;
        try {
            Optional<Account> existingAccount = accountService.getAccountById(account.getId());
            BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(
                    strength,
                    new SecureRandom());
            if (existingAccount.isPresent()) {
                String newEncodedPassword = bCrypt.encode(account.getPassword());
                account.setPassword(newEncodedPassword);
                Account newAccount = accountService.saveAccount(account);
                log.info(consoleFormat(Status.SUCCESS, "[Edit Account - DONE]  -- Status : " + HttpStatus.OK));
                return new ResponseEntity<>(newAccount, createCORSHeaders(), HttpStatus.OK);
            } else {
                log.severe(consoleFormat(Status.ERROR, "[Edit Account - KO]  -- Status : " + HttpStatus.NOT_FOUND));
                return new ResponseEntity<>(createCORSHeaders(), HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Edit Account - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR + e.getMessage()));
            return new ResponseEntity<>(createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : Tout le monde
    @PostMapping("/account/login")
    public ResponseEntity<String> login(@RequestBody Account account) {
        int strength = 10;
        try {
            log.info("In login function");
            BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(strength, new SecureRandom());
            Optional<Account> optionalAccount = accountService.getAccountByUsername(account);
            Account existingAccount;

            if (optionalAccount.isEmpty()) {
                log.warning(consoleFormat(Status.ERROR, "[Login - KO]  -- Status : " + HttpStatus.UNAUTHORIZED));
                return new ResponseEntity<>("Unmatched login", createCORSHeaders(), HttpStatus.UNAUTHORIZED);
            } else {
                existingAccount = optionalAccount.get();
            }

            if (bCrypt.matches(account.getPassword(), existingAccount.getPassword())) {
                log.info(consoleFormat(Status.SUCCESS, "[Login - DONE]  -- Status : " + HttpStatus.OK));

//                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(existingAccount.getUsername(), existingAccount.getPassword());
//                Authentication authenticated = authenticationManager.authenticate(authenticationToken);

                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                log.warning(consoleFormat(Status.ERROR, "[Login - KO]  -- Status : " + HttpStatus.UNAUTHORIZED));
                return new ResponseEntity<>("Unmatched password", createCORSHeaders(), HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR, "[Login - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            return new ResponseEntity<>("Internal Server Error", createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
