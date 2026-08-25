package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;
import org.example.mealplannerapp.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class UserTestFixtures {

    // CONSTANTS
    private static final String DEFAULT_AUTH_ID = "FakeAuthId";
    private static final String DEFAULT_USERNAME = "FakeUsername";
    private static final String DEFAULT_NICKNAME = "Alice";
    private static final Gender DEFAULT_GENDER = Gender.MALE;
    private static final Sex DEFAULT_SEX = Sex.MALE;
    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(2000, 1, 1);
    private static final BigDecimal DEFAULT_HEIGHT = new BigDecimal("180.0");

    /**
     * Builds a {@link User} entity fixture for testing.
     *
     * @return a User builder with a null {@code id} and default values in all other fields
     */
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

}