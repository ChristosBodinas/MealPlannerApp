package org.example.mealplannerapp.security;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthManagementService implements UserDetailsService {

    private final UserRepository userRepository;

    // HARDCODED
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.fetchByUsername("TestUser")
                .orElseThrow(() -> new ResourceNotFoundException("No user found with the submitted username."));
        return new AuthUser(user);
    }
}