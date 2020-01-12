package com.codeperfection.shipit.util;

import com.codeperfection.shipit.dto.SignInDto;
import com.codeperfection.shipit.dto.SignUpDto;
import com.codeperfection.shipit.dto.UserDto;
import com.codeperfection.shipit.entity.Role;
import com.codeperfection.shipit.entity.RoleName;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.security.AuthenticatedUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AuthenticationFixtureFactory {

    private static final String USERNAME = "someuser";

    private static final String PASSWORD = "somepassword";

    private static final String EMAIL = "user@email.com";

    private static final String NAME = "Some User";

    private static final UUID USER_UUID = UUID.fromString("f556e55f-6859-4d82-bc3e-9d44d8c434a3");

    private static final OffsetDateTime CREATION_DATE = OffsetDateTime.parse("2020-01-12T18:30:46.954Z");

    public static SignInDto createSignInDto() {
        return new SignInDto(USERNAME, PASSWORD);
    }

    public static SignUpDto createSignUpDto() {
        return new SignUpDto(USERNAME, PASSWORD, EMAIL, NAME);
    }

    public static UserDto createUserDto() {
        return new UserDto(USER_UUID, USERNAME, EMAIL, NAME);
    }

    public static AuthenticatedUser createAuthenticatedUser() {
        return AuthenticatedUser.builder()
                .uuid(USER_UUID)
                .username(USERNAME)
                .password(PASSWORD)
                .passwordChangeDate(CREATION_DATE)
                .authorities(List.of(new SimpleGrantedAuthority(RoleName.ROLE_USER.toString())))
                .build();
    }

    public static Role createRole() {
        return new Role(UUID.fromString("309f29b6-b530-4a58-8d42-4acf8c7a36ea"), RoleName.ROLE_USER);
    }

    public static User createUser() {
        return User.builder()
                .uuid(USER_UUID)
                .username(USERNAME)
                .password(PASSWORD)
                .passwordChangeDate(CREATION_DATE)
                .name(NAME)
                .email(EMAIL)
                .createdAt(CREATION_DATE)
                .updatedAt(CREATION_DATE)
                .roles(Collections.singleton(createRole()))
                .build();
    }
}
