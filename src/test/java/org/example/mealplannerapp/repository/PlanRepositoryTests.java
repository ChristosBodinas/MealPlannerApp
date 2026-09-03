package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDay;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlan;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PlanRepositoryTests {

    // CONSTANTS
    private static final String MY_AUTH_ID = "MyAuthId";
    private static final String MY_USERNAME = "MyUsername";
    private static final String OTHER_AUTH_ID = "OtherAuthId";
    private static final String OTHER_USERNAME = "OtherUsername";

    // BEANS
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private PlanRepository planRepository;

    // VARIABLES
    private User myUser;
    private User otherUser;

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void prepareOtherUser() {
        otherUser = defaultUser().authId(OTHER_AUTH_ID).username(OTHER_USERNAME).build();
        entityManager.persist(otherUser);
    }

    private Plan prepareDefaultPlan(User owner) {
        Plan plan = defaultPlan().user(owner).build();

        for (int i = 1; i <= 3; i++) {
            Day day = defaultDay().plan(plan).position(i).build();
            plan.getDays().add(day);
        }

        entityManager.persist(plan);
        return plan;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareUser() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        entityManager.persist(myUser);
    }

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Given an existing planId owned by the current user, " +
                "returns the requested plan and loads its days.")
        void planFetchedWithDays() {
            // Arrange
            Plan plan = prepareDefaultPlan(myUser);
            flushAndClear();

            // Act
            Optional<Plan> result = planRepository.fetchByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).as("Method output should be present.")
                    .isPresent();

            Plan fetched = result.get();
            assertThat(Hibernate.isInitialized(fetched.getDays()))
                    .as("Days should be loaded.")
                    .isTrue();
        }

        @Test
        @DisplayName("Given a non-existent planId, returns empty.")
        void planNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Plan> result = planRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing planId owned by another user, returns empty.")
        void planNotOwned() {
            // Arrange
            prepareOtherUser();
            Plan plan = prepareDefaultPlan(otherUser);
            flushAndClear();

            // Act
            Optional<Plan> result = planRepository.fetchByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

    }

    @Nested
    @DisplayName("fetchShallowByUserAndText")
    class FetchShallowByUserAndText {

        Pageable pageable;

        private Plan prepareNamedPlan(User owner, String name) {
            Plan plan = defaultPlan().user(owner).name(name).build();

            for (int i = 1; i <= 3; i++) {
                Day day = defaultDay().plan(plan).position(i).build();
                plan.getDays().add(day);
            }

            entityManager.persist(plan);
            return plan;
        }

        @BeforeEach
        void preparePageable() {
            pageable = PageRequest.of(0, 2);
        }

        @Test
        @DisplayName("Given a non-empty string, returns only those plans with at least a partial name match.")
        void onlyMatchingNameFetched() {
            // Arrange
            Plan match = prepareNamedPlan(myUser, "_A_myText_B_");
            Plan noMatch = prepareNamedPlan(myUser, "_A_nope_B_");
            flushAndClear();

            // Act
            Page<Plan> result = planRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain only the plan with the matching name.")
                    .extracting(Plan::getId).containsExactly(match.getId());
        }

        @Test
        @DisplayName("Given a non-empty string, returns only those matching plans that belong to the current user.")
        void onlyOwnedMatchesFetched() {
            // Arrange
            Plan owned = prepareNamedPlan(myUser, "_A_myText_B");
            prepareOtherUser();
            Plan notOwned = prepareNamedPlan(otherUser, "_A_myText_B_");
            flushAndClear();

            // Act
            Page<Plan> result = planRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain only the plan owned by the current user.")
                    .extracting(Plan::getId).containsExactly(owned.getId());
        }

        @Test
        @DisplayName("Does not load the days of any returned plans.")
        void daysNotLoaded() {
            // Arrange
            Plan match = prepareNamedPlan(myUser, "_A_myText_B_");
            flushAndClear();

            // Act
            Page<Plan> result = planRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain the plan.")
                    .extracting(Plan::getId).containsExactly(match.getId());

            Plan fetched = result.toList().getFirst();
            assertThat(Hibernate.isInitialized(fetched.getDays()))
                    .as("Days should not be loaded.")
                    .isFalse();
        }

        @ParameterizedTest(name = "Given a {0} string, returns all plans owned by the current user.")
        @NullAndEmptySource
        @DisplayName("Given a null or empty string, returns all plans owned by the current user.")
        void nullOrEmptyTextFetchesAll(String text) {
            // Arrange
            Plan one = prepareNamedPlan(myUser, "a");
            Plan two = prepareNamedPlan(myUser, "b");
            flushAndClear();

            // Act
            Page<Plan> result = planRepository.fetchShallowByUserAndText(myUser.getId(), text, pageable);

            // Assert
            assertThat(result).as("Method output should return all plans.")
                    .extracting(Plan::getId).containsExactlyInAnyOrder(one.getId(), two.getId());
        }
    }

    @Nested
    @DisplayName("existsByIdVerified")
    class ExistsByIdVerified {

        @Test
        @DisplayName("Given an existing planId owned by the current user, returns true.")
        void planExists() {
            // Arrange
            Plan plan = prepareDefaultPlan(myUser);
            flushAndClear();

            // Act
            boolean result = planRepository.existsByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Given a non-existent planId, returns false.")
        void planNotFound() {
            // Arrange
            flushAndClear();

            // Act
            boolean result = planRepository.existsByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Given an existing planId owned by another user, returns false.")
        void planNotOwned() {
            // Arrange
            prepareOtherUser();
            Plan plan = prepareDefaultPlan(otherUser);
            flushAndClear();

            // Act
            boolean result = planRepository.existsByIdVerified(myUser.getId(), plan.getId());

            // Assert
            assertThat(result).isFalse();
        }
    }

}

