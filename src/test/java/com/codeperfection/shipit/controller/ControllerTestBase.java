package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.security.JwtTokenProvider;
import com.codeperfection.shipit.security.SecurityUserDetailsService;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private SecurityUserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private String jwtToken = "token";

    protected AuthenticatedUser authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();

    protected void mockAuthentication() {
        final var claims = mock(Claims.class);
        final var userUuid = authenticatedUser.getUuid();
        doReturn(userUuid.toString()).when(claims).getSubject();
        doReturn(authenticatedUser).when(userDetailsService).loadUserByUuid(userUuid);
        doReturn(new Date(authenticatedUser.getPasswordChangeDate().plusSeconds(1).toInstant().toEpochMilli()))
                .when(claims).getIssuedAt();
        doReturn(claims).when(jwtTokenProvider).getTokenClaims(jwtToken);
    }

    protected String getJwtMockAuthorization() {
        return "Bearer " + jwtToken;
    }
}
