package org.example.mealplannerapp.security;

import lombok.RequiredArgsConstructor;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProvisioningService {
    private final UserRepository userRepository;

    @Transactional
    public User provisionFromJwt(Jwt jwt) {
        String authId = jwt.getSubject();
        return userRepository.findByAuthId(authId).orElseGet(() -> {
            User newUser = new User();
            newUser.setAuthId(authId);
            newUser.setUsername(jwt.getClaimAsString("preferred_username"));
            return userRepository.save(newUser);
        });
    }
}
