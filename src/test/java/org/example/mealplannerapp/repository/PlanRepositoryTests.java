package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.hibernate.Hibernate;
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
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDayBuilder;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PlanRepositoryTests {

    // BEANS
    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private TestEntityManager entityManager;

    // VARIABLES
    private User myUser;
    private User otherUser;

    // CONSTANTS
    private final String MY_USERNAME = "alice1";
    private final String OTHER_USERNAME = "bob2";

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void prepareMyUser() {
        myUser = defaultUserBuilder().username(MY_USERNAME).build();
        entityManager.persist(myUser);
    }

    private void prepareOtherUser() {
        otherUser = defaultUserBuilder().username(OTHER_USERNAME).build();
        entityManager.persist(otherUser);
    }

    private Plan preparePlan(User owner) {
        Plan plan = defaultPlanBuilder().user(owner).build();

        Day day1 = defaultDayBuilder().plan(plan).position(1).build();
        plan.getDays().add(day1);

        Day day2 = defaultDayBuilder().plan(plan).position(2).build();
        plan.getDays().add(day2);

        entityManager.persist(plan);
        return plan;
    }

    // BEFORE EACH
    @BeforeEach
    void prepareAllTests() {
        prepareMyUser();
    }

    // TESTS PROPER
    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Returns the requested plan and eagerly loads its days when it exists and belongs to the given user.")
        void planFetched() {
            // Arrange
            Plan plan = preparePlan(myUser);
            flushAndClear();

            // Act
            Optional<Plan> result = planRepository.fetchByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).isPresent();

            Plan fetched = result.get();
            assertThat(fetched.getId()).isEqualTo(plan.getId());
            assertThat(Hibernate.isInitialized(fetched.getDays())).isTrue();
        }

        @Test
        @DisplayName("Returns empty when the requested plan does not exist.")
        void planNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Plan> result = planRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Arrange
            assertThat(result).isEmpty();
            assertThat(planRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested plan exists but belongs to another user.")
        void planNotOwned() {
            // Arrange
            prepareOtherUser();
            Plan plan = preparePlan(otherUser);
            flushAndClear();

            // Act
            Optional<Plan> result = planRepository.fetchByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(planRepository.existsById(plan.getId())).isTrue();
        }

    }

    @Nested
    @DisplayName("fetchShallowByUserAndText")
    class fetchShallowByUserAndText {

        Plan preparePlanWithText(User owner, String name) {
            Plan plan = preparePlan(owner);
            plan.setName(name);
            return plan;
        }

        @BeforeEach
        void prepareTests() {
            prepareOtherUser();
        }

        @Test
        @DisplayName("Returns owned plans with at least a partial name match when given a non-empty string.")
        void ownedPlanNameMatches() {
            // Arrange
            Plan matches = preparePlanWithText(myUser, "marshmallows");
            Plan noMatch = preparePlanWithText(myUser, "marshmelons");
            Plan notOwned = preparePlanWithText(otherUser, "marshmallows");
            flushAndClear();

            // Act
            List<Plan> result = planRepository.fetchShallowByUserAndText(myUser.getId(), "mallow");

            // Assert
            assertThat(result).extracting(Plan::getId).containsExactly(matches.getId());

            Plan fetched = result.getFirst();
            assertThat(Hibernate.isInitialized(fetched.getDays())).isFalse();
        }

        @Test
        @DisplayName("Returns all owned foods when given an empty string.")
        void ownedPlanEmptyString() {
            // Arrange
            Plan owned1 = preparePlanWithText(myUser, "a");
            Plan owned2 = preparePlanWithText(myUser, "b");
            Plan notOwned = preparePlanWithText(otherUser, "c");
            flushAndClear();

            // Act
            List<Plan> result = planRepository.fetchShallowByUserAndText(myUser.getId(), "");

            // Assert
            assertThat(result).extracting(Plan::getId).containsExactlyInAnyOrder(owned1.getId(), owned2.getId());

            assertThat(Hibernate.isInitialized(result.get(0).getDays())).isFalse();
            assertThat(Hibernate.isInitialized(result.get(1).getDays())).isFalse();
        }

    }

    @Nested
    @DisplayName("existsByIdVerified")
    class ExistsByIdVerified {

        @Test
        @DisplayName("Returns true when the requested plan exists and belongs to the given user.")
        void planExists() {
            // Arrange
            Plan plan = preparePlan(myUser);
            flushAndClear();

            // Act
            boolean result = planRepository.existsByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Returns false when the requested plan does not exist.")
        void planNotFound() {
            // Arrange
            flushAndClear();

            // Act
            boolean result = planRepository.existsByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isFalse();
            assertThat(planRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns false when the requested plan exists but belongs to another user.")
        void planNotOwned() {
            // Arrange
            prepareOtherUser();
            Plan plan = preparePlan(otherUser);
            flushAndClear();

            // Act
            boolean result = planRepository.existsByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).isFalse();
            assertThat(planRepository.existsById(plan.getId())).isTrue();
        }

    }

}
