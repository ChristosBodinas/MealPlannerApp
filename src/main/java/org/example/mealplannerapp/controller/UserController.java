package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.user.UserDetailsRequest;
import org.example.mealplannerapp.dto.user.UserDetailsResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.security.IdentityService;
import org.example.mealplannerapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {

    private final IdentityService identityService;
    private final UserService userService;

    @PatchMapping("/account")
    public ResponseEntity<UserDetailsResponse> editUserDetails(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserDetailsRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        UserDetailsResponse response = userService.editUserDetails(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account")
    public ResponseEntity<UserDetailsResponse> retrieveUserDetails(
            @AuthenticationPrincipal Jwt jwt
    ) {
        User user = identityService.provisionFromJwt(jwt);
        UserDetailsResponse response = userService.retrieveUserDetails(user);
        return ResponseEntity.ok(response);
    }

}
