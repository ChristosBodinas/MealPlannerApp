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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExercise;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest(properties = { // TODO: Learn what these settings do.
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ExerciseRepositoryTests {

    // TODO: Review messages.

    // CONSTANTS
    private static final String MY_AUTH_ID = "alice_auth";
    private static final String MY_USERNAME = "Alice1";

    private static final String OTHER_AUTH_ID = "bob_auth";
    private static final String OTHER_USERNAME = "Bob2";

    // VARIABLES
    @Autowired private TestEntityManager entityManager;
    @Autowired private ExerciseRepository exerciseRepository;

    private User myUser;
    private User otherUser;

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();  // TODO: Explain.
        entityManager.clear();
    }

    private void prepareOtherUser() {
        otherUser = defaultUser().authId(OTHER_AUTH_ID).username(OTHER_USERNAME).build();
        entityManager.persist(otherUser);
    }

    private Exercise prepareExercise(User owner) {
        Exercise exercise = defaultExercise().user(owner).build();
        entityManager.persist(exercise);
        return exercise;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareMyUser() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        entityManager.persist(myUser);
    }

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Given a valid exerciseID owned by the current user, " +
                "returns the exercise and loads its associated intensity levels.")
        void validId_fetchesExerciseWithIntensityLevels() {
            // Arrange
            Exercise exercise = prepareExercise(myUser);
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).as("Method output should be present").isPresent();

            Exercise fetched = result.get();

            assertThat(fetched.getId()).as("Fetched exercise should have the correct ID.")
                    .isEqualTo(exercise.getId());

            assertThat(fetched.getUser().getId()).as("Fetched exercise should belong to the current user.")
                    .isEqualTo(myUser.getId());

            assertThat(Hibernate.isInitialized(fetched.getLevels())).as("Intensity levels should be loaded.")
                    .isTrue();
        }

        @Test
        @DisplayName("Given an exerciseId that does not exist, returns empty.")
        void idNotFound_returnsEmpty() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Exercise> result = exerciseRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an exerciseId that belongs to another user, returns empty.")
        void idNotOwned_returnsEmpty() {
            // Arrange
            prepareOtherUser();
            Exercise exercise = prepareExercise(otherUser);
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

        private Exercise prepareExerciseWithText(User owner, String name) {
            Exercise exercise = defaultExercise().user(owner).name(name).build();
            entityManager.persist(exercise);
            return exercise;
        }

        @BeforeEach
        void preparePageable() {
            pageable = PageRequest.of(0, 2);
        }

        @Test
        @DisplayName("Given a non-empty text, only returns exercises with at least a partial name match.")
        void nonEmptyText_returnsMatchingName() {
            // Arrange
            Exercise match = prepareExerciseWithText(myUser, "_A_myText_B_");
            Exercise noMatch = prepareExerciseWithText(myUser, "_A_nope_B_");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method should only return the exercise with the matching name.")
                    .extracting(Exercise::getId).containsExactly(match.getId());

        }

        @Test
        @DisplayName("Given a non-empty text, only returns matching exercises owned by the current user.")
        void nonEmptyText_returnsOnlyOwnedMatches() {
            // Arrange
            prepareOtherUser();
            Exercise owned = prepareExerciseWithText(myUser, "_A_myText_B_");
            Exercise notOwned = prepareExerciseWithText(otherUser, "A_myText_B_");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method should only return matching exercises owned by the current user.")
                    .extracting(Exercise::getId).containsExactly(owned.getId());
        }

        @Test
        @DisplayName("Does not load the associated intensity levels of returned exercises.")
        void doesNotLoadIntensityLevels() {
            // Arrange
            Exercise match = prepareExerciseWithText(myUser, "_A_myText_B_");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method should return the exercise with the matching name.")
                    .extracting(Exercise::getId).containsExactly(match.getId());

            Exercise fetched = result.toList().getFirst();
            assertThat(Hibernate.isInitialized(fetched.getLevels())).as("Intensity levels should not be loaded.")
                    .isFalse();
        }

        @Test
        @DisplayName("Given a null text, returns all exercises owned by the current user.")
        void givenNullText_returnsAllOwnedExercises() {
            // Arrange
            Exercise owned1 = prepareExerciseWithText(myUser, "a");
            Exercise owned2 = prepareExerciseWithText(myUser, "b");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), null, pageable);

            // Assert
            assertThat(result).as("Method should return all owned exercises.")
                    .extracting(Exercise::getId).containsExactlyInAnyOrder(owned1.getId(), owned2.getId());
        }

        @Test
        @DisplayName("Given an empty text, returns all exercises owned by the current user.")
        void givenEmptyText_returnsAllOwnedExercises() {
            // Arrange
            Exercise owned1 = prepareExerciseWithText(myUser, "a");
            Exercise owned2 = prepareExerciseWithText(myUser, "b");
            flushAndClear();

            // Act
            Page<Exercise> result = exerciseRepository.fetchShallowByUserAndText(myUser.getId(), "", pageable);

            // Assert
            assertThat(result).as("Method should return all owned exercises.")
                    .extracting(Exercise::getId).containsExactlyInAnyOrder(owned1.getId(), owned2.getId());
        }

    }

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {

        @Test
        @DisplayName("Given a valid exerciseId owned by the current user, " +
                "deletes the exercise and its intensity levels, then returns 1.")
        void validId_deletesExerciseAndIntensityLevels() {
            // Arrange
            Exercise exercise = prepareExercise(myUser);
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(exerciseRepository.existsById(exercise.getId()))
                    .as("Exercise should no longer exist.")
                    .isFalse();

            long levelsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"exercise_level\" WHERE \"exercise_id\" = :exerciseId")
                    .setParameter("exerciseId", exercise.getId())
                    .getSingleResult();
            assertThat(levelsCount).as("Associated intensity levels should no longer exist.").isZero();

            assertThat(result).as("Method output should be 1.").isOne();
        }

        @Test
        @DisplayName("Given an exerciseId that does not exist, returns 0.")
        void idNotFound_returnsZero() {
            // Arrange
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();
        }

        @Test
        @DisplayName("Given an exerciseId owned by another user, returns 0.")
        void idNotOwned_returnsZero() {
            // Arrange
            prepareOtherUser();
            Exercise exercise = prepareExercise(otherUser);
            flushAndClear();

            // Act
            int result = exerciseRepository.deleteByIdVerified(myUser.getId(), exercise.getId());

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();

            assertThat(exerciseRepository.existsById(exercise.getId())).as("Exercise should still exist.")
                    .isTrue();

            long levelsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"exercise_level\" WHERE \"exercise_id\" = :exerciseId")
                    .setParameter("exerciseId", exercise.getId())
                    .getSingleResult();
            assertThat(levelsCount).as("Associated intensity levels should still exist.").isNotZero();
        }

    }

}
