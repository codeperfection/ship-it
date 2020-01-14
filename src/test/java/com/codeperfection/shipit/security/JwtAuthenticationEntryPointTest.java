package com.codeperfection.shipit.security;

import com.codeperfection.shipit.exception.ErrorType;
import com.codeperfection.shipit.exception.authorization.JwtTokenInvalidatedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

public class JwtAuthenticationEntryPointTest {

    private final MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint =
            new JwtAuthenticationEntryPoint(objectMapper);

    @Test
    public void handleAuthenticationExceptionWritesErrorResponse() throws Exception {
        final var exception = new JwtTokenInvalidatedException("jwtToken");
        jwtAuthenticationEntryPoint.handleAuthenticationException(exception, httpServletResponse);

        assertThat(httpServletResponse.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.getType());
        assertThat(httpServletResponse.getStatus()).isEqualTo(SC_UNAUTHORIZED);

        final var json = objectMapper.readTree(httpServletResponse.getContentAsString());
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(OffsetDateTime.parse(json.get("timestamp").asText())).isCloseToUtcNow(epsilon);
        assertThat(json.get("status").asInt()).isEqualTo(SC_UNAUTHORIZED);
        assertThat(json.get("errorType").asText()).isEqualTo(ErrorType.UNAUTHORIZED.getDisplayName());
        assertThat(json.get("message").asText()).isEqualTo(exception.getMessage());
    }

    @Test
    public void commenceWritesErrorResponse() {
        var jwtAuthenticationEntryPoint = spy(this.jwtAuthenticationEntryPoint);
        var exception = mock(AuthenticationException.class);
        doNothing().when(jwtAuthenticationEntryPoint).handleAuthenticationException(exception, httpServletResponse);

        jwtAuthenticationEntryPoint.commence(mock(HttpServletRequest.class), httpServletResponse, exception);

        verify(jwtAuthenticationEntryPoint).handleAuthenticationException(exception, httpServletResponse);
    }
}
