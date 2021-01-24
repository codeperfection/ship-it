package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.auth.JwtResponseDto;
import com.codeperfection.shipit.dto.auth.SignInDto;
import com.codeperfection.shipit.dto.auth.SignUpDto;
import com.codeperfection.shipit.dto.auth.UserDto;
import com.codeperfection.shipit.entity.Role;
import com.codeperfection.shipit.entity.RoleName;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.InternalServerErrorException;
import com.codeperfection.shipit.exception.clienterror.EmailAlreadyTakenException;
import com.codeperfection.shipit.exception.clienterror.UsernameAlreadyTakenException;
import com.codeperfection.shipit.repository.RoleRepository;
import com.codeperfection.shipit.repository.UserRepository;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.security.JwtTokenProvider;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtTokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository,
                                 RoleRepository roleRepository, JwtTokenProvider tokenProvider,
                                 PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public JwtResponseDto generateJwtToken(SignInDto signInDto) {
        final var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signInDto.getUsernameOrEmail(), signInDto.getPassword()));
        final var authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        String jwtToken = tokenProvider.generateToken(authenticatedUser.getUuid());
        return new JwtResponseDto(jwtToken, tokenProvider.getTokenClaims(jwtToken).getSubject(),
                tokenProvider.getJwtExpirationInSeconds());
    }

    @Transactional
    public UserDto signUpUser(SignUpDto signUpDto) {
        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            throw new UsernameAlreadyTakenException(signUpDto.getUsername());
        }
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new EmailAlreadyTakenException(signUpDto.getEmail());
        }

        final var role = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new InternalServerErrorException("User role not found"));
        final var user = createUser(signUpDto, role);
        userRepository.save(user);

        return modelMapper.map(user, UserDto.class);
    }

    private User createUser(SignUpDto signUpDto, Role role) {
        final var now = OffsetDateTime.now(ZoneOffset.UTC);
        return User.builder()
                .uuid(UUID.randomUUID())
                .username(signUpDto.getUsername())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .passwordChangeDate(now)
                .name(signUpDto.getName())
                .email(signUpDto.getEmail())
                .createdAt(now)
                .updatedAt(now)
                .roles(Collections.singleton(role))
                .build();
    }
}
