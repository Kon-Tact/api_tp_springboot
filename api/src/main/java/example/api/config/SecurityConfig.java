package example.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] UNSECURED_URLs = {"/account/save", "/account/login", "/student/list", "/account/list",};
    private static final String[] USER_SECURED_URLs = {"/student/{id}", "/student/save","/student/edit",
            "/account/{id}"};
    private static  final String[] ADMIN_SECURED_URLs = {"/student/delete/{id}", "/student/clear",
            "/account/delete/{id}", "/account/clear"};

    //Secured filter chain
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
//        return http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
//                .authorizeHttpRequests((adminAuthz) -> adminAuthz
//                        .requestMatchers(UNSECURED_URLs)
//                        .permitAll()
//                        .requestMatchers(USER_SECURED_URLs)
//                        .hasAnyRole("ADMIN", "USER")
//                        .requestMatchers(ADMIN_SECURED_URLs)
//                        .hasAnyRole("ADMIN"))
//                .securityContext((context) -> context.securityContextRepository(securityContextRepository))
//
//                .build();
//    }

    //Test filter chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((testAuth) -> testAuth
                        .anyRequest().permitAll())
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Headers"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }


}
