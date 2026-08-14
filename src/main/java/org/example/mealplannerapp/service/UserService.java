package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.user.UserDetailsRequest;
import org.example.mealplannerapp.dto.user.UserDetailsResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    // editUserDetails
    @Transactional
    public UserDetailsResponse editUserDetails(User user, UserDetailsRequest request) {
        userMapper.update(user, request);
        return userMapper.toResponse(user);
    }

    // retrieveUserDetails
    public UserDetailsResponse retrieveUserDetails(User user) {
        return userMapper.toResponse(user);
    }
    
}
