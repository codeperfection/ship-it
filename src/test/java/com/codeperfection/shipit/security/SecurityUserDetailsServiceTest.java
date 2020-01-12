package com.codeperfection.shipit.security;

import com.codeperfection.shipit.exception.authorization.UserNotFoundException;
import com.codeperfection.shipit.repository.UserRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class SecurityUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUserDetailsService securityUserDetailsService;

    @Test
    public void loadUserByUsernameIfNotFoundThrowsException() {
        String nonExistingUser = "nonExistingUser";
        doReturn(Optional.empty()).when(userRepository).findByUsernameOrEmail(nonExistingUser, nonExistingUser);
        assertThatExceptionOfType(UsernameNotFoundException.class).isThrownBy(() ->
                securityUserDetailsService.loadUserByUsername(nonExistingUser));
    }

    @Test
    public void loadUserByUsernameIfFoundReturnsDto() {
        final var user = AuthenticationFixtureFactory.createUser();
        doReturn(Optional.of(user)).when(userRepository).findByUsernameOrEmail(user.getUsername(), user.getUsername());

        final var userDetails = securityUserDetailsService.loadUserByUsername(user.getUsername());
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        assertThat(userDetails).isEqualToComparingFieldByField(authenticatedUser);
    }

    @Test
    public void loadUserByUuidIfNotFoundThrowsException() {
        UUID nonExistingUuid = UUID.fromString("3c8e46d4-ae6a-46d7-8d4a-660ea5231495");
        doReturn(Optional.empty()).when(userRepository).findById(nonExistingUuid);
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() ->
                securityUserDetailsService.loadUserByUuid(nonExistingUuid));
    }

    @Test
    public void loadUserByUuidIfFoundReturnsDto() {
        final var user = AuthenticationFixtureFactory.createUser();
        doReturn(Optional.of(user)).when(userRepository).findById(user.getUuid());

        final var userDetails = securityUserDetailsService.loadUserByUuid(user.getUuid());
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        assertThat(userDetails).isEqualToIgnoringGivenFields(authenticatedUser, "password");
        assertThat(userDetails.getPassword()).isNull();
    }
}
