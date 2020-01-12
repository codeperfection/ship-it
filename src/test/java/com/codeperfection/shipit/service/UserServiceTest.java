package com.codeperfection.shipit.service;

import com.codeperfection.shipit.exception.authorization.UserNotFoundException;
import com.codeperfection.shipit.repository.UserRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    @Test
    public void getCurrentUserIfNotFoundThrowsException() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(userRepository).findById(authenticatedUser.getUuid());
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() ->
                userService.getCurrentUser(authenticatedUser));
    }

    @Test
    public void getCurrentUserIfFoundReturnsDto() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = AuthenticationFixtureFactory.createUser();
        doReturn(Optional.of(user)).when(userRepository).findById(authenticatedUser.getUuid());

        final var userDto = userService.getCurrentUser(authenticatedUser);
        assertThat(userDto).isEqualToComparingFieldByField(user);
    }
}
