package com.codeperfection.shipit.security;

import com.codeperfection.shipit.exception.authorization.UserNotFoundException;
import com.codeperfection.shipit.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SecurityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        final var user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(() ->
                new UsernameNotFoundException("User with username/email not found: " + usernameOrEmail));
        final var authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().toString())).collect(Collectors.toList());
        return AuthenticatedUser.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .password(user.getPassword())
                .passwordChangeDate(user.getPasswordChangeDate())
                .authorities(authorities)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser loadUserByUuid(UUID uuid) {
        final var user = userRepository.findById(uuid).orElseThrow(() -> new UserNotFoundException(uuid));
        final var authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().toString())).collect(Collectors.toList());
        return AuthenticatedUser.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .passwordChangeDate(user.getPasswordChangeDate())
                .authorities(authorities)
                .build();
    }
}
