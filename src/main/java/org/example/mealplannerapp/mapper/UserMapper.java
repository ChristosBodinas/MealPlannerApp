package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.user.UserDetailsRequest;
import org.example.mealplannerapp.dto.user.UserDetailsResponse;
import org.example.mealplannerapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // TODO: Ignore authId and username.
    void update(@MappingTarget User user, UserDetailsRequest request);

    UserDetailsResponse toResponse(User user);
    
}
