package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Exercise;
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
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExerciseBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ExerciseRepositoryTests {

    // BEANS
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExerciseRepository exerciseRepository;

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

    private Exercise prepareExercise(User owner) {
        Exercise exercise = defaultExerciseBuilder().user(owner).build();
        entityManager.persist(exercise);
        return exercise;
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
        @DisplayName("Returns the requested exercise and eagerly loads its levels when it exists and belongs to the given user.")
        void exerciseFetched() {
            // Arrange
            Exercise exercise = prepareExercise(myUser);
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).isPresent();
            Exercise fetched = result.get();

            assertThat(fetched.getId()).isEqualTo(exercise.getId());
            assertThat(fetched.getUser().getId()).isEqualTo(myUser.getId());
            assertThat(Hibernate.isInitialized(fetched.getLevels())).isTrue();
        }

        @Test
        @DisplayName("Returns empty when the requested exercise does not exist.")
        void exerciseNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(exerciseRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested exercise exists but belongs to a different user.")
        void exerciseNotOwned() {
            // Arrange
            prepareOtherUser();
            Exercise exercise = prepareExercise(otherUser);
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(exerciseRepository.existsById(exercise.getId())).isTrue();
        }

    }

    @Nested
    @DisplayName("fetchShallowByUserAndText")
    class FetchShallowByUserAndText {

        private Exercise prepareExerciseWithText(User owner, String name) {
            Exercise exercise = defaultExerciseBuilder().user(owner).name(name).build();
            entityManager.persist(exercise);
            return exercise;
        }

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
            prepareOtherUser();
        }

        @Test
        @DisplayName("Returns owned exercises with at least a partial name match when given a non-empty string.")
        void ownedExerciseNameMatches() {
            // Arrange
            Exercise match = prepareExerciseWithText(myUser, "weight lifting");
            Exercise noMatch = prepareExerciseWithText(myUser, "feather lifting");
            Exercise notOwned = prepareExerciseWithText(otherUser, "weight dropping");
            flushAndClear();

            // Act
            List<Exercise> results = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "weight");

            // Assert
            assertThat(results).extracting(Exercise::getId).containsExactly(match.getId());
            Exercise fetched = results.get(0);

            assertThat(Hibernate.isInitialized(fetched.getLevels())).isFalse();
        }

        @Test
        @DisplayName("Returns all owned exercises when given an empty string.")
        void ownedExerciseEmptyString() {
            // Arrange
            Exercise owned1 = prepareExerciseWithText(myUser, "a");
            Exercise owned2 = prepareExerciseWithText(myUser, "b");
            Exercise notOwned = prepareExerciseWithText(otherUser, "c");
            flushAndClear();

            // Act
            List<Exercise> results = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "");

            // Assert
            assertThat(results).extracting(Exercise::getId).containsExactlyInAnyOrder(owned1.getId(), owned2.getId());
            assertThat(Hibernate.isInitialized(results.get(0).getLevels())).isFalse();
            assertThat(Hibernate.isInitialized(results.get(1).getLevels())).isFalse();
        }

    }

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Deletes the requested exercise and its associated levels when it exists and belongs to the given user.")
        void exerciseDeleted() {
            // Arrange
            Exercise exercise = prepareExercise(myUser);
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).isOne();
            assertThat(exerciseRepository.existsById(exercise.getId())).isFalse();

            Long levelsCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"exercise_level\" WHERE \"exercise_id\" = :exerciseId")
                    .setParameter("exerciseId", exercise.getId())
                    .getSingleResult();
            assertThat(levelsCount).isZero();
        }

        @Test
        @DisplayName("Does nothing when the requested exercise does not exist.")
        void exerciseNotFound() {
            // Arrange
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isZero();
            assertThat(exerciseRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Does nothing when the requested exercise exists but belongs to a different user.")
        void exerciseNotOwned() {
            // Arrange
            prepareOtherUser();
            Exercise exercise = prepareExercise(otherUser);
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).isZero();
            assertThat(exerciseRepository.existsById(exercise.getId())).isTrue();

            Long levelsCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"exercise_level\" WHERE \"exercise_id\" = :exerciseId")
                    .setParameter("exerciseId", exercise.getId())
                    .getSingleResult();
            assertThat(levelsCount).isNotZero();
        }

    }

}
