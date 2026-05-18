package com.example.utils;

import com.example.entity.User;
import com.example.service.UserService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    CurrentUserProvider(@Lazy UserService userService, AuthenticationContext authenticationContext) {
        this.userService = userService;
        this.authenticationContext = authenticationContext;
    }

    public User getCurrentUser() {
        return authenticationContext
                .getAuthenticatedUser(org.springframework.security.core.userdetails.UserDetails.class)
                .flatMap(userDetails -> userService.findByUsername(userDetails.getUsername()))
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

}
