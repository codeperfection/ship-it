package com.codeperfection.shipit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private String jwtSecret;

    private long jwtExpirationMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expiration-millis}") long jwtExpirationMillis) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMillis = jwtExpirationMillis;
    }

    public String generateToken(UUID userUuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMillis);
        return Jwts.builder()
                .setSubject(userUuid.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public Claims getTokenClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    public long getJwtExpirationInSeconds() {
        return jwtExpirationMillis / 1000;
    }
}
