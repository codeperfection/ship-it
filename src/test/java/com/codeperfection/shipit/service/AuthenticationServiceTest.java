package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.auth.JwtResponseDto;
import com.codeperfection.shipit.dto.auth.UserDto;
import com.codeperfection.shipit.entity.RoleName;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EmailAlreadyTakenException;
import com.codeperfection.shipit.exception.clienterror.UsernameAlreadyTakenException;
import com.codeperfection.shipit.repository.RoleRepository;
import com.codeperfection.shipit.repository.UserRepository;
import com.codeperfection.shipit.security.JwtTokenProvider;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void generateJwtTokenIfInvalidCredentialsThrowsException() {
        final var signInDto = AuthenticationFixtureFactory.createSignInDto();
        final var authentication = new UsernamePasswordAuthenticationToken(signInDto.getUsernameOrEmail(),
                signInDto.getPassword());
        doThrow(mock(AuthenticationException.class)).when(authenticationManager).authenticate(authentication);
        assertThatExceptionOfType(AuthenticationException.class).isThrownBy(() ->
                authenticationService.generateJwtToken(signInDto));

        verifyNoMoreInteractions(authenticationManager, userRepository, roleRepository,
                tokenProvider, passwordEncoder, modelMapper);
    }

    @Test
    public void generateJwtTokenIfValidCredentialsReturnsDto() {
        final var authentication = mock(Authentication.class);
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(authenticatedUser).when(authentication).getPrincipal();
        final var signInDto = AuthenticationFixtureFactory.createSignInDto();
        doReturn(authentication).when(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(
                signInDto.getUsernameOrEmail(), signInDto.getPassword()));

        String token = "someJwtToken";
        doReturn(token).when(tokenProvider).generateToken(authenticatedUser.getUuid());

        String subject = "authenticatedUserSubject";
        final var tokenClaims = mock(Claims.class);
        doReturn(subject).when(tokenClaims).getSubject();
        doReturn(tokenClaims).when(tokenProvider).getTokenClaims(token);

        long expirationInSeconds = 10;
        doReturn(expirationInSeconds).when(tokenProvider).getJwtExpirationInSeconds();

        final var jwtResponseDto = authenticationService.generateJwtToken(signInDto);

        assertThat(jwtResponseDto).isEqualTo(new JwtResponseDto(token, subject, expirationInSeconds));

        verifyNoMoreInteractions(authenticationManager, userRepository, roleRepository,
                tokenProvider, passwordEncoder, modelMapper);
    }

    @Test
    public void signUpUserIfExistingUsernameThrowsException() {
        final var signUpDto = AuthenticationFixtureFactory.createSignUpDto();
        doReturn(true).when(userRepository).existsByUsername(signUpDto.getUsername());
        assertThatExceptionOfType(UsernameAlreadyTakenException.class).isThrownBy(() ->
                authenticationService.signUpUser(signUpDto));

        verifyNoMoreInteractions(authenticationManager, userRepository, roleRepository,
                tokenProvider, passwordEncoder, modelMapper);
    }

    @Test
    public void signUpUserIfExistingEmailThrowsException() {
        final var signUpDto = AuthenticationFixtureFactory.createSignUpDto();
        doReturn(false).when(userRepository).existsByUsername(signUpDto.getUsername());
        doReturn(true).when(userRepository).existsByEmail(signUpDto.getEmail());
        assertThatExceptionOfType(EmailAlreadyTakenException.class).isThrownBy(() ->
                authenticationService.signUpUser(signUpDto));

        verifyNoMoreInteractions(authenticationManager, userRepository, roleRepository,
                tokenProvider, passwordEncoder, modelMapper);
    }

    @Test
    public void signUpUserIfValidUserSavesInRepository() {
        final var role = AuthenticationFixtureFactory.createRole();
        doReturn(Optional.of(role)).when(roleRepository).findByName(RoleName.ROLE_USER);
        String encodedPassword = "encodedPassword";
        final var signUpDto = AuthenticationFixtureFactory.createSignUpDto();
        doReturn(false).when(userRepository).existsByUsername(signUpDto.getUsername());
        doReturn(false).when(userRepository).existsByEmail(signUpDto.getEmail());
        doReturn(encodedPassword).when(passwordEncoder).encode(signUpDto.getPassword());

        final var userDto = authenticationService.signUpUser(signUpDto);
        final var expectedUserDto = AuthenticationFixtureFactory.createUserDto();
        assertThat(userDto).isEqualToIgnoringGivenFields(expectedUserDto, "uuid");

        final var userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        final var savedUser = userArgumentCaptor.getValue();
        assertThat(userDto).isEqualToComparingFieldByField(savedUser);
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.getRoles()).containsOnly(role);
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(savedUser.getPasswordChangeDate()).isCloseToUtcNow(epsilon);
        assertThat(savedUser.getCreatedAt()).isCloseToUtcNow(epsilon);
        assertThat(savedUser.getUpdatedAt()).isCloseToUtcNow(epsilon);

        verify(modelMapper).map(savedUser, UserDto.class);
        verifyNoMoreInteractions(authenticationManager, userRepository, roleRepository,
                tokenProvider, passwordEncoder, modelMapper);
    }
}
