package com.codeperfection.shipit.security;

import com.codeperfection.shipit.exception.ApiError;
import com.codeperfection.shipit.exception.ErrorType;
import com.codeperfection.shipit.exception.InternalServerErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                         AuthenticationException exception) {
        handleAuthenticationException(exception, httpServletResponse);
    }

    public void handleAuthenticationException(Exception exception, HttpServletResponse response) {
        logger.error(exception.getMessage(), exception.getCause());
        try {
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            response.setStatus(SC_UNAUTHORIZED);
            final var printWriter = response.getWriter();
            objectMapper.writeValue(printWriter, new ApiError(OffsetDateTime.now(ZoneOffset.UTC), SC_UNAUTHORIZED,
                    ErrorType.UNAUTHORIZED, exception.getMessage()));
            printWriter.flush();
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while sending response", e);
        }
    }
}
