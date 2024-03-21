package example.api.config;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtTokenProvider {

    private final SecretKey secretKey;

    public JwtTokenProvider(SecretKey secretKey) {
        this.secretKey = secretKey;
    }
    private Set<String> blacklistedTokens = new HashSet<>();

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateToken(Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        try {
            // Build JWT claims
            Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
            //claims.put("roles", roles);

            // Generate JWT token with the claims
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(secretKey)
                    .compact();
        } catch (Exception e) {
            return null;
        }
    }

    public Authentication getAuthentication(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        return new UsernamePasswordAuthenticationToken(username, null, null);
    }

    public boolean validateToken(String token) {

        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void invalidateToken(String token) {
        // Add the token to the blacklist
        blacklistedTokens.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        // Check if the token is in the blacklist
        return blacklistedTokens.contains(token);
    }
}
