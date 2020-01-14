package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.exception.ErrorType;
import com.codeperfection.shipit.service.UserService;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.codeperfection.shipit.controller.RequestValues.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends ControllerTestBase {

    @MockBean
    private UserService userService;

    @Test
    public void getCurrentUserIfNotAuthenticatedReturnsError() throws Exception {
        mockMvc.perform(get(API_V1 + USERS + ME)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.UNAUTHORIZED.getDisplayName())));
    }

    @Test
    public void getCurrentUserIfAuthenticatedReturnsDto() throws Exception {
        mockAuthentication();
        final var userDto = AuthenticationFixtureFactory.createUserDto();
        doReturn(userDto).when(userService).getCurrentUser(authenticatedUser);
        mockMvc.perform(get(API_V1 + USERS + ME)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(userDto)));
        verify(userService).getCurrentUser(authenticatedUser);
        verifyNoMoreInteractions(userService);
    }
}
