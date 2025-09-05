package com.ofss.util;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    // This secret key is used to digitally sign the token.
    private final String SECRET_KEY = "stockmanager-secret";

    // You want sessions to expire after 10 minutes.
    private final long EXPIRATION_TIME = 10 * 60 * 1000;

    // When user logs in, we give them a token using this(GENERATE TOKEN)
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) // Set the user identity
                .setIssuedAt(new Date()) // Token issue time
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Token expiry
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Sign with key
                .compact(); // Final string token
    }

    // To get email/username from token when verifying it later(EXTRACT EMAIL)
    public String extractEmail(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validate token's integrity and check if it's not expired
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
