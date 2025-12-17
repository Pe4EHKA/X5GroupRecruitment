package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.UserInfoDto;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for user info and login management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Authentication and user info endpoints")
public class AuthController {

    private final UserService userService;

    @Operation(
        summary = "Get current user info",
        description = "Returns information about the currently authenticated user including roles"
    )
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        
        UserInfoDto userInfo = UserInfoDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .displayName(user.getFullName())
            .roles(user.getRoles())
            .build();
        
        return ResponseEntity.ok(userInfo);
    }
}
