package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Exercise;
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
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExercise;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ExerciseRepositoryTests {

    // CONSTANTS
    private static final String MY_AUTH_ID = "MyAuthId";
    private static final String MY_USERNAME = "MyUsername";
    private static final String OTHER_AUTH_ID = "OtherAuthId";
    private static final String OTHER_USERNAME = "OtherUsername";

    // BEANS
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ExerciseRepository exerciseRepository;

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

    private Exercise prepareDefaultExercise(User owner) {
        Exercise exercise = defaultExercise().user(owner).build();
        entityManager.persist(exercise);
        return exercise;
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
        @DisplayName("Given an existing exerciseId owned by the current user, " +
                "returns the exercise and loads its associated effort levels.")
        void exerciseFetchedWithLevels() {
            // Arrange
            Exercise exercise = prepareDefaultExercise(myUser);
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();

            Exercise fetched = result.get();
            assertThat(Hibernate.isInitialized(fetched.getLevels())).as("Effort levels should be loaded.")
                    .isTrue();
        }

        @Test
        @DisplayName("Given a non-existent exerciseId, returns empty.")
        void exerciseNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing exerciseId owned by another user, returns empty.")
        void exerciseNotOwned() {
            // Arrange
            prepareOtherUser();
            Exercise exercise = prepareDefaultExercise(otherUser);
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchShallowByUserAndText")
    class FetchShallowByUserAndText {

        private Pageable pageable;

        private Exercise prepareNamedExercise(User owner, String name) {
            Exercise exercise = defaultExercise().user(owner).name(name).build();
            entityManager.persist(exercise);
            return exercise;
        }

        @BeforeEach
        void preparePageable() {
            pageable = PageRequest.of(0, 2);
        }

        @Test
        @DisplayName("Given a non-empty string, returns only those exercises with at least a partial name match.")
        void onlyMatchingNameFetched() {
            // Arrange
            Exercise match = prepareNamedExercise(myUser, "_A_myText_B_");
            Exercise noMatch = prepareNamedExercise(myUser, "_A_nope_B_");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain only the exercise with the matching name.")
                    .extracting(Exercise::getId).containsExactly(match.getId());
        }

        @Test
        @DisplayName("Given a non-empty string, returns only those matching exercises that belong to the current user.")
        void onlyOwnedMatchesFetched() {
            // Arrange
            Exercise owned = prepareNamedExercise(myUser, "_A_myText_B_");
            prepareOtherUser();
            Exercise notOwned = prepareNamedExercise(otherUser, "_A_myText_B_");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain only the exercise owned by the current user.")
                    .extracting(Exercise::getId).containsExactly(owned.getId());
        }

        @Test
        @DisplayName("Does not load the effort levels of any returned exercises.")
        void levelsNotLoaded() {
            // Arrange
            Exercise exercise = prepareNamedExercise(myUser, "_A_myText_B_");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain the exercise.")
                    .extracting(Exercise::getId).containsExactly(exercise.getId());

            Exercise fetched = result.toList().getFirst();
            assertThat(Hibernate.isInitialized(fetched.getLevels()))
                    .as("Effort levels should not be loaded.")
                    .isFalse();
        }

        @ParameterizedTest(name = "Given a {0} string, returns all exercises owned by the current user.")
        @NullAndEmptySource
        @DisplayName("Given a null or empty string, returns all exercises owned by the current user.")
        void nullOrEmptyTextFetchesAll(String text) {
            // Arrange
            Exercise one = prepareNamedExercise(myUser, "a");
            Exercise two = prepareNamedExercise(myUser, "b");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), text, pageable);

            // Assert
            assertThat(result).as("Method output should return all exercises.")
                    .extracting(Exercise::getId).containsExactlyInAnyOrder(one.getId(), two.getId());
        }

    }

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {

        @Test
        @DisplayName("Given an existing exerciseId owned by the current user, " +
                "deletes the exercise and its associated effort levels, then returns 1.")
        void exerciseAndLevelsDeleted() {
            // Arrange
            Exercise exercise = prepareDefaultExercise(myUser);
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).as("Method output should be 1.").isOne();

            assertThat(exerciseRepository.existsById(exercise.getId()))
                    .as("Exercise should no longer exist.")
                    .isFalse();

            long levelsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"effort_level\" WHERE \"exercise_id\" = :exerciseId")
                    .setParameter("exerciseId", exercise.getId())
                    .getSingleResult();
            assertThat(levelsCount).as("Associated effort levels should no longer exist.").isZero();
        }

        @Test
        @DisplayName("Given a non-existent exerciseId, returns 0.")
        void exerciseNotFound() {
            // Arrange
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();
        }

        @Test
        @DisplayName("Given an existing exerciseId owned by another user, returns 0.")
        void exerciseNotOwned() {
            // Arrange
            prepareOtherUser();
            Exercise exercise = prepareDefaultExercise(otherUser);
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();

            assertThat(exerciseRepository.existsById(exercise.getId()))
                    .as("Exercise should still exist.")
                    .isTrue();

            long levelsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"effort_level\" WHERE \"exercise_id\" = :exerciseId")
                    .setParameter("exerciseId", exercise.getId())
                    .getSingleResult();
            assertThat(levelsCount).as("Associated effort levels should still exist.").isNotZero();
        }

    }

}
