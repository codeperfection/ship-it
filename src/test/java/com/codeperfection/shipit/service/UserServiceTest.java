package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.auth.UserDto;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.authorization.UserNotFoundException;
import com.codeperfection.shipit.exception.clienterror.IncorrectPasswordException;
import com.codeperfection.shipit.repository.UserRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    @Test
    public void getCurrentUser_IfNotFound_ThrowsException() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(userRepository).findById(authenticatedUser.getUuid());
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() ->
                userService.getCurrentUser(authenticatedUser));
        verifyNoMoreInteractions(userRepository, modelMapper);
    }

    @Test
    public void getCurrentUser_IfFound_ReturnsDto() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = AuthenticationFixtureFactory.createUser();
        doReturn(Optional.of(user)).when(userRepository).findById(authenticatedUser.getUuid());

        final var userDto = userService.getCurrentUser(authenticatedUser);
        assertThat(userDto).usingRecursiveComparison().isEqualTo(user);

        verify(modelMapper).map(user, UserDto.class);
        verifyNoMoreInteractions(userRepository, modelMapper);
    }

    @Test
    public void changePassword_IfNotFound_ThrowsException() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(userRepository).findById(authenticatedUser.getUuid());
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() ->
                userService.changePassword(AuthenticationFixtureFactory.createChangePasswordDto(), authenticatedUser));
        verifyNoMoreInteractions(userRepository, modelMapper, passwordEncoder);
    }

    @Test
    public void changePassword_IfIncorrectPassword_ThrowsException() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = AuthenticationFixtureFactory.createUser();
        final var changePasswordDto = AuthenticationFixtureFactory.createChangePasswordDto();
        doReturn(Optional.of(user)).when(userRepository).findById(authenticatedUser.getUuid());
        doReturn(false).when(passwordEncoder).matches(changePasswordDto.getOldPassword(), user.getPassword());
        assertThatExceptionOfType(IncorrectPasswordException.class).isThrownBy(() ->
                userService.changePassword(AuthenticationFixtureFactory.createChangePasswordDto(), authenticatedUser));
        verifyNoMoreInteractions(userRepository, modelMapper, passwordEncoder);
    }

    @Test
    public void changePassword_IfCorrectPassword_SavesNewPassword() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = AuthenticationFixtureFactory.createUser();
        final var changePasswordDto = AuthenticationFixtureFactory.createChangePasswordDto();
        String newEncodedPassword = "newEncodedPassword";
        doReturn(Optional.of(user)).when(userRepository).findById(authenticatedUser.getUuid());
        doReturn(newEncodedPassword).when(passwordEncoder).encode(changePasswordDto.getNewPassword());
        doReturn(true).when(passwordEncoder).matches(changePasswordDto.getOldPassword(), user.getPassword());

        userService.changePassword(AuthenticationFixtureFactory.createChangePasswordDto(), authenticatedUser);

        final var userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        final var savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser).usingRecursiveComparison().ignoringFields("password", "passwordChangeDate", "updatedAt")
                .isEqualTo(user);
        assertThat(savedUser.getPassword()).isEqualTo(newEncodedPassword);
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(savedUser.getPasswordChangeDate()).isCloseToUtcNow(epsilon);
        assertThat(savedUser.getUpdatedAt()).isCloseToUtcNow(epsilon);
        verifyNoMoreInteractions(userRepository, modelMapper, passwordEncoder);
    }
}
