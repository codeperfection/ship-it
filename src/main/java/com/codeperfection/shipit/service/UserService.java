package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.UserDto;
import com.codeperfection.shipit.exception.authorization.UserNotFoundException;
import com.codeperfection.shipit.repository.UserRepository;
import com.codeperfection.shipit.security.AuthenticatedUser;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private UserRepository userRepository;

    private ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public UserDto getCurrentUser(AuthenticatedUser authenticatedUser) {
        return userRepository.findById(authenticatedUser.getUuid()).map(user -> modelMapper.map(user, UserDto.class))
                .orElseThrow(() -> new UserNotFoundException(authenticatedUser.getUuid()));
    }
}
