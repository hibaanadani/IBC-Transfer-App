package com.IBCSolutions.TransferApp.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // CRITICAL: Get secret from application.properties/yml
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // CRITICAL: Get expiration time from application.properties/yml (e.g., 600000 ms = 10 minutes)
    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Generates a JWT token based on the authenticated user.
     * FIX: Uses the signature required by your AuthService call (userId and Authentication).
     * The User ID is passed to be stored directly in the token's subject field.
     */
    public String generateToken(Long userId, Authentication authentication) {
        // Retrieve the UserDetails from the Authentication object's principal
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Build the token
        return Jwts.builder()
                // Set the token's subject to the User ID (Long), which is unique and immutable
                .setSubject(userId.toString())
                // Add the username as a custom claim for convenience
                .claim("username", userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the User ID from the token's subject field.
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // The subject is the User ID we stored as a String
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Validates the integrity and expiration of the JWT token.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}