package com.codeperfection.shipit.security;

import com.codeperfection.shipit.exception.authorization.JwtTokenInvalidatedException;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityUserDetailsService userDetailsService;

    private final JwtTokenProvider jwtTokenProvider;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    JwtAuthenticationFilter(SecurityUserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider,
                            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        final String jwtToken = getJwtFromRequest(request);
        if (jwtToken == null) {
            // There are endpoints in the service which don't require authentication
            filterChain.doFilter(request, response);
            return;
        }

        try {
            SecurityContextHolder.getContext().setAuthentication(getAuthentication(jwtToken));
        } catch (Exception exception) {
            jwtAuthenticationEntryPoint.handleAuthenticationException(exception, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        final Claims claims = jwtTokenProvider.getTokenClaims(token);
        final UUID userUuid = UUID.fromString(claims.getSubject());
        final AuthenticatedUser user = userDetailsService.loadUserByUuid(userUuid);
        if (user.getPasswordChangeDate().isAfter(claims.getIssuedAt().toInstant().atOffset(ZoneOffset.UTC))) {
            throw new JwtTokenInvalidatedException(token);
        }

        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String bearerPrefix = "Bearer ";
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(bearerPrefix)) {
            return bearerToken.substring(bearerPrefix.length());
        }
        return null;
    }
}
