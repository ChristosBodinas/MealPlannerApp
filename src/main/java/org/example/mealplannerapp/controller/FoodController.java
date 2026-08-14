package org.example.mealplannerapp.controller;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.security.UserProvisioningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class FoodController {

    private final UserProvisioningService userProvisioningService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/me") public ResponseEntity<String> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        User user = userProvisioningService.provisionFromJwt(jwt);
        return ResponseEntity.ok(user.getUsername());
    }
}
