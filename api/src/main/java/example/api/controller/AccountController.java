package example.api.controller;

import example.api.config.JwtTokenProvider;
import example.api.model.Account;
import example.api.service.AccountService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
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
public class AccountController {

    public enum Status {
        SUCCESS,
        ERROR
    }

    @Autowired
    public JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthenticationManager authenticationManager;

    public AccountController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
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

    @RequestMapping(value = "/account/login", method = {RequestMethod.OPTIONS})
    public ResponseEntity<Account> handleOptionsSaveAccount() {
        log.info("Option de POST appelée");
        try {
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/account/delete", method = {RequestMethod.OPTIONS})
    public ResponseEntity<Account> handleOptionsDeleteAccount() {
        try {
            return new ResponseEntity<>( HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : Admin
    //@PreAuthorize("hasAuthority('ADMIN')")
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
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @PostMapping("/account/role")
    public String getAccountByUsername(@RequestBody String username) {
        log.info(username);
        try {
            Account account = accountService.getAccountByUsername(username);
            String role = account.getRoles().toString().replace("[", "").replace("]", "");;
            String successText = "{\"role\":\""+ role + "\"}";
            log.info(role);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Get Account - DONE]  -- Status : " + HttpStatus.OK));
            return successText;
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Get Account - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : Admin
    //@PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/account/delete/")
    public ResponseEntity<String> deleteAccountById(@RequestParam final Long id) {
        try {
            accountService.deleteAccountById(id);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Delete Account - DONE]  -- Status : " + HttpStatus.OK));
            String successMessage = "{\"Status\": \"200\", \"Méthode\": \"Delete account\"}";
            return new ResponseEntity<>(successMessage, HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Base clear - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            String errorMessage = "{\"Status\": \"Error\", \"Méthode\": \"Delete account\"}";
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    //Niveau d'autorisation : Admin
    //@PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/account/clear")
    public ResponseEntity<String> deleteAllAccounts(@Nonnull HttpServletRequest request) {
        try {

            //Disconnect the account
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                jwtTokenProvider.invalidateToken(token);
            }

            accountService.deleteAllAccounts();

            //Create a generic admin account
            Account generic = new Account("admin", "admin", "admin@email.com");
            accountService.saveAccount(generic);

            log.info(consoleFormat(Status.SUCCESS,
                    "[Delete all accounts - DONE]  -- Status : " + HttpStatus.OK));
            String successMessage = "{\"Status\": \"200\", \"Méthode\": \"Delete all accounts\"}";

            return new ResponseEntity<>(successMessage, HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Delete all accounts - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            String errorMessage = "{\"Status\": \"Error\", \"Méthode\": \"Delete all account\"}";
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : Tout le monde
    @PostMapping("/account/save")
    public ResponseEntity<Account> saveAccount(@RequestBody Account account) {
        log.info("Méthode save Account appelée");

        try {
            accountService.saveAccount(account);
            log.info(consoleFormat(Status.SUCCESS, "[Save Account - DONE]  -- Status : " + HttpStatus.OK));
            return new ResponseEntity<>(account, HttpStatus.OK);
        } catch (Exception e) {

            log.severe("Exception during saveAccount: " + e.getMessage());
            return new ResponseEntity<>(account, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : User
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @PutMapping("/account/edit")
    public ResponseEntity<Account> editAccount(@RequestBody Account account) {
        try {
            Optional<Account> existingAccount = accountService.getAccountById(account.getId());
            BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(10, new SecureRandom());
            if (existingAccount.isPresent()) {
                String newEncodedPassword = bCrypt.encode(account.getPassword());
                account.setPassword(newEncodedPassword);
                Account newAccount = accountService.saveAccount(account);
                log.info(consoleFormat(Status.SUCCESS, "[Edit Account - DONE]  -- Status : " + HttpStatus.OK));
                return new ResponseEntity<>(newAccount, HttpStatus.OK);
            } else {
                log.severe(consoleFormat(Status.ERROR, "[Edit Account - KO]  -- Status : " + HttpStatus.NOT_FOUND));
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Edit Account - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR + e.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : Tout le monde
    //Uniquement utilisé pour des tests
    @GetMapping("/account/actual")
    public String getCurrentAccount() throws AccountNotFoundException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info(SecurityContextHolder.getContext().toString());
        String username = (String) authentication.getPrincipal();
        log.info(username);
        Account account = accountService.getAccountByUsername(username);

        if(username == null) {
            return ("There's no connected account");
        }
        return "The connected account is " + username + ". Authorities : " + account.getAuthorities();
    }

    @PostMapping("/account/login")
    public ResponseEntity<String> login(@RequestBody Account account) {
        try {
            log.info("In login function");
            // Authenticate the user (e.g., check credentials against database)
            BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(10, new SecureRandom());
            Account existingAccount = accountService.getAccountByUsername(account.getUsername());

            if (existingAccount == null || !bCrypt.matches(account.getPassword(), existingAccount.getPassword())) {
                return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
            }

            UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(
                    account.getUsername(),
                    account.getPassword()
            );
            Authentication authentication = authenticationManager.authenticate(userToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Getting role of the existing account for authorization management in front
            String role = existingAccount
                    .getRoles()
                    .toString()
                    .replace("[", "")
                    .replace("]", "");

            // Return the token and the role in the response
            log.info(token);
            log.info(role);
            String jsonResponse = "{" +
                    "\"token\":\"" + token + "\"," +
                    "\"role\": \"" + role + "\"," +
                    "\"email\":\"" + existingAccount.getEmail() + "\"," +
                    "\"id\":\"" + existingAccount.getId() + "\"" +
                    "}";
            log.info(jsonResponse);
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Login - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR + e.getMessage()));
            return new ResponseEntity<>("The logout method did not worked", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("account/logout")
    public ResponseEntity<String> logout(@Nonnull HttpServletRequest request) {
        log.info("in logout");
        try {
            String bearerToken = request.getHeader("Authorization");
            String token = bearerToken.substring(7); // Remove "Bearer " prefix
            log.info(token);
            jwtTokenProvider.invalidateToken(token);

            String jsonResponse = "{\"token\":\"The account has been disconnected and the token is no more valid\"}";
            log.info(consoleFormat(Status.SUCCESS, "[Logout - DONE]  -- Status : " + HttpStatus.OK));
            return new ResponseEntity<>(jsonResponse , HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Logout - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR + e.getMessage()));
            return new ResponseEntity<>("The logout method did not worked", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
