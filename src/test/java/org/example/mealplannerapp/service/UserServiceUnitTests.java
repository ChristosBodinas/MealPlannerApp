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
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

/**
 * Unit tests for the {@link UserService} methods using a real (non-mocked)
 * {@link UserMapper} instance.
 */
public class UserServiceUnitTests {

    // VARIABLES
    private UserService userService;
    private UserMapper userMapper;

    private User myUser;

    // HELPER METHODS
    private Gender anyOtherGender(Gender initialGender) {
        return EnumSet.allOf(Gender.class)
                .stream()
                .filter(gender -> gender != initialGender)
                .findFirst()
                .orElse(null);
    }

    private Sex anyOtherSex(Sex initialSex) {
        return initialSex == Sex.MALE ? Sex.FEMALE : Sex.MALE;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareServiceAndUser() {
        userMapper = new UserMapperImpl();
        userService = new UserService(userMapper);

        myUser = defaultUser().build();
    }

    @Nested
    @DisplayName("editUserDetails")
    class EditUserDetails {

        @Test
        @DisplayName("Updates the current user's details to match request data and returns the updated details.")
        void detailsEdited() {
            // Arrange
            UserDetailsRequest request = UserDetailsRequest.builder()
                    .nickname(myUser.getNickname() + "_edited")
                    .gender(anyOtherGender(myUser.getGender()))
                    .sex(anyOtherSex(myUser.getSex()))
                    .birthDate(myUser.getBirthDate().plusYears(3))
                    .height(myUser.getHeight().add(new BigDecimal("15.0")))
                    .build();

            // Act
            UserDetailsResponse response = userService.editUserDetails(myUser, request);

            // Assert
            assertThat(myUser)
                    .as("User details should now match request data.")
                    .usingRecursiveComparison()
                    .ignoringFields("id", "authId", "username")
                    .isEqualTo(request);

            assertThat(response)
                    .as("Method output should match mapper output.")
                    .isEqualTo(userMapper.toResponse(myUser));
        }

    }

    @Nested
    @DisplayName("retrieveUserDetails")
    class RetrieveUserDetails {

        @Test
        @DisplayName("Returns the current user's details.")
        void detailsRetrieved() {
            // Act
            UserDetailsResponse response = userService.retrieveUserDetails(myUser);

            // Assert
            assertThat(response)
                    .as("Method output should match mapper output.")
                    .isEqualTo(userMapper.toResponse(myUser));
        }

    }

}