package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.JwtResponseDto;
import com.codeperfection.shipit.dto.SignInDto;
import com.codeperfection.shipit.dto.SignUpDto;
import com.codeperfection.shipit.dto.UserDto;
import com.codeperfection.shipit.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(RequestValues.API_V1)
public class AuthenticationController {

    private AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(RequestValues.SIGN_IN)
    public ResponseEntity<JwtResponseDto> signIn(@Valid @RequestBody SignInDto signInDto) {
        return ResponseEntity.ok(authenticationService.generateJwtToken(signInDto));
    }

    @PostMapping(RequestValues.SIGN_UP)
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        UserDto userDto = authenticationService.signUpUser(signUpDto);
        return ResponseEntity.ok(userDto);
    }
}
