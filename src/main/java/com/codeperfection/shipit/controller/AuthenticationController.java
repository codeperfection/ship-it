package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.auth.JwtResponseDto;
import com.codeperfection.shipit.dto.auth.SignInDto;
import com.codeperfection.shipit.dto.auth.SignUpDto;
import com.codeperfection.shipit.dto.auth.UserDto;
import com.codeperfection.shipit.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(CommonPathValues.API_V1)
public class AuthenticationController {

    public static final String SIGN_IN_PATH = "/sign-in";

    public static final String SIGN_UP_PATH = "/sign-up";

    private AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(SIGN_IN_PATH)
    public ResponseEntity<JwtResponseDto> signIn(@Valid @RequestBody SignInDto signInDto) {
        return ResponseEntity.ok(authenticationService.generateJwtToken(signInDto));
    }

    @PostMapping(SIGN_UP_PATH)
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        UserDto userDto = authenticationService.signUpUser(signUpDto);
        return ResponseEntity.ok(userDto);
    }
}
