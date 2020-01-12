package com.codeperfection.shipit.security;

import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtTokenProviderTest {

    private final String jwtSecret = "someSecret";

    private final long jwtExpirationMillis = 20_000;

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtSecret, jwtExpirationMillis);

    @Test
    public void generateTokenWithAuthenticationReturnsValidToken() {
        UUID userUuid = AuthenticationFixtureFactory.createAuthenticatedUser().getUuid();
        String token = jwtTokenProvider.generateToken(userUuid);
        var claims = jwtTokenProvider.getTokenClaims(token);

        assertThat(claims.getSubject()).isEqualTo(userUuid.toString());
        long epsilon = 10_000;
        Date now = new Date();
        assertThat(claims.getIssuedAt()).isCloseTo(now, epsilon);
        assertThat(claims.getExpiration()).isCloseTo(new Date(now.getTime() + jwtExpirationMillis), epsilon);
    }

    @Test
    public void getJwtExpirationInSecondsReturnsCorrectNumber() {
        assertThat(jwtTokenProvider.getJwtExpirationInSeconds()).isEqualTo(20);
    }
}
