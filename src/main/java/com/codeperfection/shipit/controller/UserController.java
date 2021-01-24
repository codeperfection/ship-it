package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.auth.ChangePasswordDto;
import com.codeperfection.shipit.dto.auth.UserDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(CommonPathValues.API_V1 + UserController.USERS_PATH + UserController.ME_PATH)
public class UserController {

    static final String USERS_PATH = "/users";

    static final String ME_PATH = "/me";

    static final String PASSWORD_PATH = "/password";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(userService.getCurrentUser(authenticatedUser));
    }

    @PutMapping(PASSWORD_PATH)
    public void changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto,
                               @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        userService.changePassword(changePasswordDto, authenticatedUser);
    }
}
