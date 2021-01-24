package com.codeperfection.shipit.security;

import com.codeperfection.shipit.exception.InternalServerErrorException;
import com.codeperfection.shipit.exception.errordto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                         AuthenticationException exception) {
        handleAuthenticationException(exception, httpServletResponse);
    }

    /**
     * Handler for non-AuthenticationException types, which occurred in the process of authentication.
     */
    public void handleAuthenticationException(Exception exception, HttpServletResponse response) {
        log.error(exception.getMessage(), exception.getCause());
        try {
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            response.setStatus(SC_UNAUTHORIZED);
            final var printWriter = response.getWriter();
            objectMapper.writeValue(printWriter, ApiError.unauthorized(exception.getMessage()));
            printWriter.flush();
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while sending response", e);
        }
    }
}
