package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.auth.SignInDto;
import com.codeperfection.shipit.dto.auth.SignUpDto;
import com.codeperfection.shipit.exception.errordto.ErrorType;
import com.codeperfection.shipit.service.AuthenticationService;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static com.codeperfection.shipit.controller.CommonPathValues.API_V1;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthenticationControllerTest extends ControllerTestBase {

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    public void signInIfInvalidPayloadReturnsError() throws Exception {
        final var signInDto = new SignInDto("", "");
        mockMvc.perform(post(API_V1 + AuthenticationController.SIGN_IN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_PAYLOAD.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("usernameOrEmail", "password")));
        verifyNoInteractions(authenticationService);
    }

    @Test
    public void signInIfValidPayloadReturnsToken() throws Exception {
        final var signInDto = AuthenticationFixtureFactory.createSignInDto();
        final var jwtResponseDto = AuthenticationFixtureFactory.createJwtResponseDto();
        doReturn(jwtResponseDto).when(authenticationService).generateJwtToken(signInDto);
        mockMvc.perform(post(API_V1 + AuthenticationController.SIGN_IN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(jwtResponseDto)));
        verify(authenticationService).generateJwtToken(signInDto);
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    public void signUpIfInvalidPayloadReturnsError() throws Exception {
        final var signUpDto = new SignUpDto("!@#", "p", "invalidEmail", "");
        mockMvc.perform(post(API_V1 + AuthenticationController.SIGN_UP_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_PAYLOAD.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("username", "password", "email", "name")));
        verifyNoInteractions(authenticationService);
    }

    @Test
    public void signUpIfValidPayloadReturnsUserDto() throws Exception {
        final var signUpDto = AuthenticationFixtureFactory.createSignUpDto();
        final var userDto = AuthenticationFixtureFactory.createUserDto();
        doReturn(userDto).when(authenticationService).signUpUser(signUpDto);
        mockMvc.perform(post(API_V1 + AuthenticationController.SIGN_UP_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(userDto)));
        verify(authenticationService).signUpUser(signUpDto);
        verifyNoMoreInteractions(authenticationService);
    }
}
