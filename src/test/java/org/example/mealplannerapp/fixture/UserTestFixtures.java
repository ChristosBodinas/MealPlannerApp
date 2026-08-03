package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;
import org.example.mealplannerapp.entity.User;

import java.time.LocalDate;

public class UserTestFixtures {

    private static final String DEFAULT_USERNAME = "bill123";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_NICKNAME = "Billy";
    private static final Gender DEFAULT_GENDER = Gender.MALE;
    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1995, 5, 12);
    private static final Sex DEFAULT_SEX = Sex.MALE;
    private static final double DEFAULT_HEIGHT = 180;

    /**
     * Method for building {@link User} entities.
     *
     * @return a User builder with all fields except {@code id} filled out
     */
    public static User.UserBuilder defaultUserBuilder() {
        return User.builder()
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_PASSWORD)
                .nickname(DEFAULT_NICKNAME)
                .gender(DEFAULT_GENDER)
                .birthDate(DEFAULT_BIRTH_DATE)
                .sex(DEFAULT_SEX)
                .height(DEFAULT_HEIGHT);
    }
}