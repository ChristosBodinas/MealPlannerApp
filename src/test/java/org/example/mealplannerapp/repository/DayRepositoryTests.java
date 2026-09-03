package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDay;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlan;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class DayRepositoryTests {

    // CONSTANTS
    private static final String MY_AUTH_ID = "MyAuthId";
    private static final String MY_USERNAME = "MyUsername";
    private static final String OTHER_AUTH_ID = "OtherAuthId";
    private static final String OTHER_USERNAME = "OtherUsername";

    // BEANS
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private DayRepository dayRepository;

    // VARIABLES
    private User myUser;
    private Plan myPlan;
    private User otherUser;
    private Plan otherPlan;

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void prepareOtherUserAndPlan() {
        otherUser = defaultUser().authId(OTHER_AUTH_ID).username(OTHER_USERNAME).build();
        otherPlan = defaultPlan().user(otherUser).build();

        entityManager.persist(otherUser);
        entityManager.persist(otherPlan);
    }

    private Day prepareDay(Plan plan) {
        Day day = defaultDay().plan(plan)
                .position(plan.getDays().size())
                .build();

        entityManager.persist(day);
        return day;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareUserAndPlan() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        myPlan = defaultPlan().user(myUser).build();

        entityManager.persist(myUser);
        entityManager.persist(myPlan);
    }

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Given an existing dayId owned by the current user, returns the requested day.")
        void dayFetched() {
            // Arrange
            Day day = prepareDay(myPlan);
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();
        }

        @Test
        @DisplayName("Given a non-existent dayId, returns empty.")
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing dayId owned by another user, returns empty.")
        void dayNotOwned() {
            // Arrange
            prepareOtherUserAndPlan();
            Day day = prepareDay(otherPlan);
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

    }

    @Nested
    @DisplayName("existsByIdVerified")
    class ExistsByIdVerified {

        @Test
        @DisplayName("Given an existing dayId owned by the current user, returns true.")
        void dayExists() {
            // Arrange
            Day day = prepareDay(myPlan);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).as("Method output should be true.").isTrue();
        }

        @Test
        @DisplayName("Given a non-existent dayId, returns false.")
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be false.").isFalse();
        }

        @Test
        @DisplayName("Given an existing dayId owned by another user, returns false.")
        void dayNotOwned() {
            // Arrange
            prepareOtherUserAndPlan();
            Day day = prepareDay(otherPlan);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).as("Method output should be false.").isFalse();
        }
    }
}
