package com.codeperfection.shipit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(UUID userUuid) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + jwtProperties.getExpirationMillis());
        return Jwts.builder()
                .setSubject(userUuid.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    public Claims getTokenClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    public long getJwtExpirationInSeconds() {
        return jwtProperties.getExpirationMillis() / 1000;
    }
}
