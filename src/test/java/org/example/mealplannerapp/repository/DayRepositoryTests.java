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

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private TestEntityManager entityManager;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private User currentUser;
    private User otherUser;
    private Plan currentUserPlan;
    private Plan otherUserPlan;

    @BeforeEach
    void prepareAllTests() {
        currentUser = defaultUserBuilder().username("alice").build();
        otherUser = defaultUserBuilder().username("bob").build();

        currentUserPlan = defaultPlanBuilder().user(currentUser).build();
        otherUserPlan = defaultPlanBuilder().user(otherUser).build();

        entityManager.persist(currentUser);
        entityManager.persist(otherUser);
        entityManager.persist(currentUserPlan);
        entityManager.persist(otherUserPlan);
    }

    @Nested
    class fetchByIdVerified {

        @Test
        @DisplayName("Returns the requested day when it exists and belongs to the given user.")
        void dayFetched() {
            // Arrange
            Day day = defaultDayBuilder().plan(currentUserPlan).build();
            currentUserPlan.getDays().add(day);
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(currentUser.getId(), day.getId());

            // Assert
            assertThat(result).isPresent();
            Day found = result.get();
            assertThat(found.getId()).isEqualTo(day.getId());
            assertThat(found.getPlan().getUser().getId()).isEqualTo(currentUser.getId());
        }

        @Test
        @DisplayName("Returns empty if the requested day does not exist.")
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(currentUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(dayRepository.findById(999L)).isEmpty();
        }

        @Test
        @DisplayName("Returns empty if the requested day exists but does not belong to the given user.")
        void dayNotOwned() {
            // Arrange
            Day day = defaultDayBuilder().plan(otherUserPlan).build();
            otherUserPlan.getDays().add(day);
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(currentUser.getId(), day.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(dayRepository.findById(day.getId())).isPresent();
        }

    }

    @Nested
    class existsByIdVerified {

        @Test
        @DisplayName("Returns true if the requested day exists and belongs to the given user.")
        void dayExists() {
            // Arrange
            Day day = defaultDayBuilder().plan(currentUserPlan).build();
            currentUserPlan.getDays().add(day);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(currentUser.getId(), day.getId());

            // Assert
            assertThat(result).isTrue();
            assertThat(dayRepository.existsById(day.getId())).isTrue();
        }

        @Test
        @DisplayName("Returns false if the requested day does not exist.")
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(currentUser.getId(), 999L);

            // Assert
            assertThat(result).isFalse();
            assertThat(dayRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns false if the requested day exists but does not belong to the given user.")
        void dayNotOwned() {
            // Arrange
            Day day = defaultDayBuilder().plan(otherUserPlan).build();
            otherUserPlan.getDays().add(day);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(currentUser.getId(), day.getId());

            // Assert
            assertThat(result).isFalse();
            assertThat(dayRepository.existsById(day.getId())).isTrue();
        }

    }
}