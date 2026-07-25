package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.UserTestFixtures.*;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.*;

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

    private User currentUser;
    private User otherUser;
    private Plan currentUserPlan;
    private Plan otherUserPlan;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @BeforeEach
    void prepareAllTests() {
        currentUser = defaultUserBuilder().username("William59").build();
        otherUser = defaultUserBuilder().username("Jack37").build();

        currentUserPlan = defaultPlanBuilder().user(currentUser).build();
        otherUserPlan = defaultPlanBuilder().user(otherUser).build();

        entityManager.persist(currentUser);
        entityManager.persist(otherUser);
        entityManager.persist(currentUserPlan);
        entityManager.persist(otherUserPlan);
    }

    // TODO: Add proper display names for each test.

    @Nested
    class fetchByIdVerified {
        
        @Test
        void dayFetched() {
            // Arrange
            Day day = defaultDayBuilder().plan(currentUserPlan).build();
            currentUserPlan.setDays(List.of(day));
            entityManager.persist(day);
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(currentUser.getId(), day.getId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(day.getId());
            assertThat(result.get().getPlan().getUser().getId()).isEqualTo(currentUser.getId());
        }

        @Test
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Day> result = dayRepository.fetchByIdVerified(currentUser.getId(), 99999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(dayRepository.findById(99999L)).isEmpty();
        }

        @Test
        void dayNotOwned() {
            // Arrange
            Day day = defaultDayBuilder().plan(otherUserPlan).build();
            otherUserPlan.setDays(List.of(day));
            entityManager.persist(day);
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
        void dayExists() {
            // Arrange
            Day day = defaultDayBuilder().plan(currentUserPlan).build();
            currentUserPlan.setDays(List.of(day));
            entityManager.persist(day);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(currentUser.getId(), day.getId());

            // Assert
            assertThat(result).isEqualTo(true);
        }

        @Test
        void dayNotFound() {
            // Arrange
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(currentUser.getId(), 99999L);

            // Assert
            assertThat(result).isEqualTo(false);
            assertThat(dayRepository.existsById(99999L)).isEqualTo(false);
        }

        @Test
        void dayNotOwned() {
            // Arrange
            Day day = defaultDayBuilder().plan(otherUserPlan).build();
            otherUserPlan.setDays(List.of(day));
            entityManager.persist(day);
            flushAndClear();

            // Act
            boolean result = dayRepository.existsByIdVerified(currentUser.getId(), day.getId());

            // Assert
            assertThat(result).isEqualTo(false);
            assertThat(dayRepository.existsById(day.getId())).isEqualTo(true);
        }

    }

}