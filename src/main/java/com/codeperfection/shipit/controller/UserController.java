package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.ChangePasswordDto;
import com.codeperfection.shipit.dto.UserDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(RequestValues.API_V1 + RequestValues.USERS + RequestValues.ME)
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(userService.getCurrentUser(authenticatedUser));
    }

    @PutMapping(RequestValues.PASSWORD)
    public void changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto,
                               @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        userService.changePassword(changePasswordDto, authenticatedUser);
    }
}
