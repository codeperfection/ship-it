package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.auth.ChangePasswordDto;
import com.codeperfection.shipit.exception.errordto.ErrorType;
import com.codeperfection.shipit.service.UserService;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.codeperfection.shipit.controller.CommonPathValues.API_V1;
import static com.codeperfection.shipit.controller.UserController.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends ControllerTestBase {

    @MockBean
    private UserService userService;

    @Test
    public void getCurrentUserIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(get(API_V1 + USERS_PATH + ME_PATH));
    }

    @Test
    public void getCurrentUserIfAuthenticatedReturnsDto() throws Exception {
        mockAuthentication();
        final var userDto = AuthenticationFixtureFactory.createUserDto();
        doReturn(userDto).when(userService).getCurrentUser(authenticatedUser);
        mockMvc.perform(get(API_V1 + USERS_PATH + ME_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(userDto)));
        verify(userService).getCurrentUser(authenticatedUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void changePasswordIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(get(API_V1 + USERS_PATH + ME_PATH + PASSWORD_PATH));
    }

    @Test
    public void changePasswordIfInvalidPayloadReturnsError() throws Exception {
        mockAuthentication();
        final var invalidChangePasswordDto = new ChangePasswordDto("", "abd");
        mockMvc.perform(put(API_V1 + USERS_PATH + ME_PATH + PASSWORD_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidChangePasswordDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_PAYLOAD.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("oldPassword", "newPassword")));
        verifyNoInteractions(userService);
    }

    @Test
    public void changePasswordIfAuthenticatedReturnsOk() throws Exception {
        mockAuthentication();
        final var changePasswordDto = AuthenticationFixtureFactory.createChangePasswordDto();
        doNothing().when(userService).changePassword(changePasswordDto, authenticatedUser);
        mockMvc.perform(put(API_V1 + USERS_PATH + ME_PATH + PASSWORD_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful());
        verify(userService).changePassword(changePasswordDto, authenticatedUser);
        verifyNoMoreInteractions(userService);
    }
}
