package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.auth.ChangePasswordDto;
import com.codeperfection.shipit.dto.auth.UserDto;
import com.codeperfection.shipit.exception.authorization.UserNotFoundException;
import com.codeperfection.shipit.exception.clienterror.IncorrectPasswordException;
import com.codeperfection.shipit.repository.UserRepository;
import com.codeperfection.shipit.security.AuthenticatedUser;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public UserDto getCurrentUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(authenticatedUser.getUuid()).map(user -> modelMapper.map(user, UserDto.class))
                .orElseThrow(() -> new UserNotFoundException(authenticatedUser.getUuid()));
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void changePassword(ChangePasswordDto changePasswordDto, AuthenticatedUser authenticatedUser) {
        final var user = userRepository.findById(authenticatedUser.getUuid())
                .orElseThrow(() -> new UserNotFoundException(authenticatedUser.getUuid()));
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        final var now = OffsetDateTime.now();
        user.setPasswordChangeDate(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
    }
}
