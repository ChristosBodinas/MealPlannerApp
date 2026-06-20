package org.example.mealplannerapp.security;

import lombok.AllArgsConstructor;
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
        return new AuthUser(userRepository.findByUsername("TestUser"));
    }
}
