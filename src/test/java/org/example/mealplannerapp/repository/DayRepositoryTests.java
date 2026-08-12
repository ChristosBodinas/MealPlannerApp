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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDayBuilder;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class DayRepositoryTests {

    // BEANS
    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private TestEntityManager entityManager;

    // VARIABLES
    private User myUser;
    private User otherUser;
    private Plan myPlan;
    private Plan otherPlan;

    // CONSTANTS
    private final String MY_USERNAME = "alice1";
    private final String OTHER_USERNAME = "bob2";

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private User lookupUser(Day day) {
        return day.getPlan().getUser();
    }

    private void prepareMyUser() {
        myUser = defaultUserBuilder().username(MY_USERNAME).build();
        myPlan = defaultPlanBuilder().user(myUser).build();

        entityManager.persist(myUser);
        entityManager.persist(myPlan);
    }

    private void prepareOtherUser() {
        otherUser = defaultUserBuilder().username(OTHER_USERNAME).build();
        otherPlan = defaultPlanBuilder().user(otherUser).build();

        entityManager.persist(otherUser);
        entityManager.persist(otherPlan);
    }

    private Day prepareDay(Plan ownerPlan) {
        Day day = defaultDayBuilder().plan(ownerPlan).build();
        ownerPlan.getDays().add(day);
        return day;
    }

    // TESTS PROPER
    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Returns the requested day when it exists and belongs to the given user.")
        void dayFetched() {
            // Arrange
            Day day = prepareDay(myPlan);
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).isPresent();
            Day fetched = result.get();

            assertThat(fetched.getId()).isEqualTo(day.getId());
            assertThat(lookupUser(fetched).getId()).isEqualTo(myUser.getId());
        }

        @Test
        @DisplayName("Returns empty when the requested day does not exist.")
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(dayRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested day exists but belongs to a different user.")
        void dayNotOwned() {
            // Arrange
            prepareOtherUser();
            Day day = prepareDay(otherPlan);
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(dayRepository.existsById(day.getId())).isTrue();
        }

    }

    @Nested
    @DisplayName("existsByIdVerified")
    class ExistsByIdVerified {

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Returns true when the requested day exists and belongs to the given user.")
        void dayExists() {
            // Arrange
            Day day = prepareDay(myPlan);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).isTrue();
            assertThat(dayRepository.existsById(day.getId())).isTrue();
        }

        @Test
        @DisplayName("Returns false when the requested day does not exist.")
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isFalse();
            assertThat(dayRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns false when the requested day exists but belongs to a different user.")
        void dayNotOwned() {
            // Arrange
            prepareOtherUser();
            Day day = prepareDay(otherPlan);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(myUser.getId(), day.getId());

            // Assert
            assertThat(result).isFalse();
            assertThat(dayRepository.existsById(day.getId())).isTrue();
        }

    }

}