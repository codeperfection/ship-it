package com.codeperfection.shipit.security;

import com.codeperfection.shipit.exception.authorization.InvalidJwtTokenException;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityUserDetailsService userDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void doFilterInternalIfNoJwtInRequestContinuesChain() throws IOException, ServletException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtAuthenticationEntryPoint, never()).handleAuthenticationException(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void doFilterInternalIfTokenIssuedAfterPasswordResetThrowsException() throws IOException, ServletException {
        final var authenticatedUser = mockAuthenticatedUser();
        var tokenClaims = mock(Claims.class);
        mockTokenClaims(tokenClaims, authenticatedUser);
        doReturn(new Date(authenticatedUser.getPasswordChangeDate().minusSeconds(1).toInstant().toEpochMilli()))
                .when(tokenClaims).getIssuedAt();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtAuthenticationEntryPoint).handleAuthenticationException(any(InvalidJwtTokenException.class), any());
        verify(filterChain, never()).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void doFilterInternalIfValidTokenSetsAuthentication() throws IOException, ServletException {
        final var authenticatedUser = mockAuthenticatedUser();
        var tokenClaims = mock(Claims.class);
        mockTokenClaims(tokenClaims, authenticatedUser);
        doReturn(new Date(authenticatedUser.getPasswordChangeDate().plusSeconds(1).toInstant().toEpochMilli()))
                .when(tokenClaims).getIssuedAt();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtAuthenticationEntryPoint, never()).handleAuthenticationException(any(), any());
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(
                new UsernamePasswordAuthenticationToken(authenticatedUser, null,
                        authenticatedUser.getAuthorities()));
    }

    private void mockTokenClaims(Claims tokenClaims, AuthenticatedUser authenticatedUser) {
        String token = "someToken";
        doReturn("Bearer " + token).when(request).getHeader(HttpHeaders.AUTHORIZATION);
        doReturn(authenticatedUser.getUuid().toString()).when(tokenClaims).getSubject();
        doReturn(tokenClaims).when(jwtTokenProvider).getTokenClaims(token);
    }

    private AuthenticatedUser mockAuthenticatedUser() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(authenticatedUser).when(userDetailsService).loadUserByUuid(authenticatedUser.getUuid());
        return authenticatedUser;
    }
}
