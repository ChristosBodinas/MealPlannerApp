package org.example.mealplannerapp.security;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final UserRepository userRepository;

    @Transactional
    public User provisionFromJwt(Jwt jwt) {
        String authId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");

        User presentUser = userRepository.fetchByAuthId(authId).orElseGet(() -> {
            User newUser = new User();
            newUser.setAuthId(authId);
            newUser.setUsername(username);
            return userRepository.save(newUser);
        });

        presentUser.setUsername(username);  // Update username if it has been changed.

        return presentUser;
    }
}
