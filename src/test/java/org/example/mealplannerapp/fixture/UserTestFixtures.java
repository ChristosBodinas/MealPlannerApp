package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;
import org.example.mealplannerapp.dto.user.UserDetailsRequest;
import org.example.mealplannerapp.dto.user.UserDetailsResponse;
import org.example.mealplannerapp.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class UserTestFixtures {

    // CONSTANTS
    private final static String DEFAULT_AUTH_ID = "FakeAuthId";
    private final static String DEFAULT_USERNAME = "FakeUsername";
    private final static String DEFAULT_NICKNAME = "Alice";
    private final static Gender DEFAULT_GENDER = Gender.MALE;
    private final static Sex DEFAULT_SEX = Sex.MALE;
    private final static LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(2000, 1, 1);
    private final static BigDecimal DEFAULT_HEIGHT = new BigDecimal("180.0");

    public static User.UserBuilder defaultUser() {
        return User.builder()
                .authId(DEFAULT_AUTH_ID)
                .username(DEFAULT_USERNAME)
                .nickname(DEFAULT_NICKNAME)
                .gender(DEFAULT_GENDER)
                .sex(DEFAULT_SEX)
                .birthDate(DEFAULT_BIRTH_DATE)
                .height(DEFAULT_HEIGHT);
    }

    public static UserDetailsRequest.UserDetailsRequestBuilder defaultUserDetailsRequest() {
        return UserDetailsRequest.builder()
                .nickname(DEFAULT_NICKNAME)
                .gender(DEFAULT_GENDER)
                .sex(DEFAULT_SEX)
                .birthDate(DEFAULT_BIRTH_DATE)
                .height(DEFAULT_HEIGHT);
    }

    public static UserDetailsResponse.UserDetailsResponseBuilder defaultUserDetailsResponse() {
        return UserDetailsResponse.builder()
                .username(DEFAULT_USERNAME)
                .nickname(DEFAULT_NICKNAME)
                .gender(DEFAULT_GENDER)
                .sex(DEFAULT_SEX)
                .birthDate(DEFAULT_BIRTH_DATE)
                .height(DEFAULT_HEIGHT);
    }
}
