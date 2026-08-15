package org.example.mealplannerapp.service;

import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;
import org.example.mealplannerapp.dto.user.UserDetailsRequest;
import org.example.mealplannerapp.dto.user.UserDetailsResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.mapper.UserMapper;
import org.example.mealplannerapp.mapper.UserMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.UserTestFixtures.*;

public class UserServiceUnitTests {

    private UserService userService;
    private UserMapper userMapper;
    private User myUser;

    @BeforeEach
    void prepareAllTests() {
        userMapper = new UserMapperImpl();
        userService = new UserService(userMapper);
        myUser = defaultUser().build();
    }

    @Nested
    @DisplayName("editUserDetails")
    class EditUserDetails {
        private UserDetailsRequest request;

        private Gender otherGender(Gender currentGender) {
            return EnumSet.allOf(currentGender.getDeclaringClass())
                    .stream()
                    .filter(gender -> gender != currentGender)
                    .findFirst()
                    .orElse(null);
        }

        @BeforeEach
        void prepareTests() {

            request = UserDetailsRequest.builder()
                    .nickname(myUser.getNickname() + "_edited")
                    .gender(otherGender(myUser.getGender()))
                    .sex(myUser.getSex() == Sex.MALE ? Sex.FEMALE : Sex.MALE)
                    .birthDate(myUser.getBirthDate().plusYears(3))
                    .height(myUser.getHeight().add(new BigDecimal("10.0")))
                    .build();
        }

        @Test
        @DisplayName("Edits user details.")
        void detailsEdited() {
            // Act
            UserDetailsResponse response = userService.editUserDetails(myUser, request);

            // AssertThat
            assertThat(myUser)
                    .as("User details now match request data.")
                    .usingRecursiveComparison()
                    .ignoringFields("id", "authId", "username")
                    .isEqualTo(request);

            assertThat(response)
                    .as("Method output matches mapper output.")
                    .isEqualTo(userMapper.toResponse(myUser));
        }

    }

    @Nested
    @DisplayName("retrieveUserDetails")
    class RetrieveUserDetails {

        @Test
        @DisplayName("Returns user details.")
        void detailsRetrieved() {
            // Arrange

            // Act
            UserDetailsResponse response = userService.retrieveUserDetails(myUser);

            // Assert
            assertThat(response)
                    .as("Method output matches mapper output.")
                    .isEqualTo(userMapper.toResponse(myUser));
        }

    }

}
