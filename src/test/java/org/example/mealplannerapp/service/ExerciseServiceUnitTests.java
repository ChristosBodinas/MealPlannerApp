package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.DuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.ExerciseMapper;
import org.example.mealplannerapp.mapper.ExerciseMapperImpl;
import org.example.mealplannerapp.repository.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExercise;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExerciseRequest;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link ExerciseService} methods using a mocked
 * {@link ExerciseRepository} and a real (non-mocked) {@link ExerciseMapper}
 * interface.
 */
@ExtendWith(MockitoExtension.class)
public class ExerciseServiceUnitTests {

    // CONSTANTS
    private static final long USER_ID = 1L;
    private static final long EXERCISE_ID = 99L;

    // BEANS
    private ExerciseService exerciseService;
    private ExerciseMapper exerciseMapper;
    @Mock
    private ExerciseRepository exerciseRepository;

    // VARIABLES
    private User myUser;

    // HELPER METHODS
    private ExerciseRequest prepareInvalidRequest() {
        Set<LevelRequest> invalidLevels = new HashSet<>(Set.of(
                new LevelRequest("dupe", new BigDecimal("5.0")),
                new LevelRequest("dupe", new BigDecimal("1.0"))));

        return ExerciseRequest.builder()
                .name("Invalid Exercise")
                .levels(invalidLevels)
                .build();
    }

    // TESTS PROPER
    @BeforeEach
    void prepareServiceAndUser() {
        exerciseMapper = new ExerciseMapperImpl();
        exerciseService = new ExerciseService(exerciseRepository, exerciseMapper);

        myUser = defaultUser().id(USER_ID).build();
    }

    @Nested
    @DisplayName("createExercise")
    class CreateExercise {

        @Test
        @DisplayName("Given a valid request, creates a new exercise owned by the current user, " +
                "and returns a response.")
        void exerciseCreated() {
            // Arrange
            ExerciseRequest request = defaultExerciseRequest().build();
            Exercise saved = defaultExercise().id(EXERCISE_ID).user(myUser).build();

            when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);

            // Act
            ExerciseResponse response = exerciseService.createExercise(myUser, request);

            // Assert
            assertThat(response).as("Method output should match mapper output")
                    .isEqualTo(exerciseMapper.toResponse(saved));

            ArgumentCaptor<Exercise> captor = ArgumentCaptor.forClass(Exercise.class);
            verify(exerciseRepository, description("New exercise should be saved to the database."))
                    .save(captor.capture());
            Exercise created = captor.getValue();

            assertThat(created.getUser()).as("New exercise should belong to the current user")
                    .isEqualTo(myUser);
            assertThat(created).as("New exercise fields should match request data.")
                    .usingRecursiveComparison()
                    .ignoringFields("id", "user")
                    .isEqualTo(request);
        }

        @Test
        @DisplayName("Given a request with duplicate level names, throws a DuplicateValueException.")
        void duplicateLevelNames() {
            // Arrange
            ExerciseRequest request = prepareInvalidRequest();

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.createExercise(myUser, request))
                    .as("Method should throw a DuplicateValueException")
                    .isInstanceOf(DuplicateValueException.class);

            verify(exerciseRepository, never().description("Nothing should be saved to the database."))
                    .save(any(Exercise.class));
        }

    }

    @Nested
    @DisplayName("updateExercise")
    class UpdateExercise {

        private ExerciseRequest prepareValidRequest(Exercise exercise) {
            Set<LevelRequest> updatedLevels = new HashSet<>(exercise.getLevels().size());

            for (EffortLevel level : exercise.getLevels()) {
                updatedLevels.add(new LevelRequest(level.getName() + "_edited",
                        level.getBurnRate().add(new BigDecimal("5.0"))));
            }

            return ExerciseRequest.builder()
                    .name(exercise.getName() + "_edited")
                    .levels(updatedLevels)
                    .build();
        }

        @Test
        @DisplayName("Given an existing exerciseId owned by the current user and a valid request, " +
                "updates the requested exercise and returns a response.")
        void exerciseUpdated() {
            // Arrange
            Exercise fetched = defaultExercise().id(EXERCISE_ID).user(myUser).build();
            ExerciseRequest request = prepareValidRequest(fetched);

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID))
                    .thenReturn(Optional.of(fetched));

            // Act
            ExerciseResponse response = exerciseService.updateExercise(myUser, EXERCISE_ID, request);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(exerciseMapper.toResponse(fetched));

            assertThat(fetched).as("Updated exercise fields should match request fields.")
                    .usingRecursiveComparison()
                    .ignoringFields("id", "user")
                    .isEqualTo(request);

        }

        @Test
        @DisplayName("Given a request with duplicate level names, throws a DuplicateValueException.")
        void duplicateLevelNames() {
            // Arrange
            ExerciseRequest request = prepareInvalidRequest();

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.updateExercise(myUser, EXERCISE_ID, request))
                    .as("Method should throw a DuplicateValueException.")
                    .isInstanceOf(DuplicateValueException.class);
        }

        @Test
        @DisplayName("Given a non-existent or non-owned exerciseId, throws a ResourceNotFoundException.")
        void exerciseNotFound() {
            // Arrange
            ExerciseRequest request = defaultExerciseRequest().build();

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.updateExercise(myUser, EXERCISE_ID, request))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("deleteExercise")
    class DeleteExercise {

        @Test
        @DisplayName("Given an existing exerciseId owned by the current user, deletes the requested exercise.")
        void exerciseDeleted() {
            // Arrange
            when(exerciseRepository.deleteByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(1);

            // Act + Assert
            assertThatCode(() -> exerciseService.deleteExercise(myUser, EXERCISE_ID))
                    .as("Method should not throw any exceptions.")
                    .doesNotThrowAnyException();

            verify(exerciseRepository, description("deleteByIdVerified should be called."))
                    .deleteByIdVerified(USER_ID, EXERCISE_ID);
        }

        @Test
        @DisplayName("Given a non-existent or non-owned exerciseId, throws a ResourceNotFoundException.")
        void exerciseNotFound() {
            // Arrange
            when(exerciseRepository.deleteByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(0);

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.deleteExercise(myUser, EXERCISE_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("retrieveExercise")
    class RetrieveExercise {

        @Test
        @DisplayName("Given an existing exerciseId owned by the current user, returns a response.")
        void exerciseRetrieved() {
            // Arrange
            Exercise fetched = defaultExercise().id(EXERCISE_ID).user(myUser).build();

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID))
                    .thenReturn(Optional.of(fetched));

            // Act
            ExerciseResponse response = exerciseService.retrieveExercise(myUser, EXERCISE_ID);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(exerciseMapper.toResponse(fetched));
        }

        @Test
        @DisplayName("Given a non-existent or non-owned exerciseId, throws a ResourceNotFoundException.")
        void exerciseNotFound() {
            // Arrange
            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.retrieveExercise(myUser, EXERCISE_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("searchExercises")
    class SearchExercises {

        @Test
        @DisplayName("Returns a page of listed responses.")
        void matchesReturned() {
            // Arrange
            List<Exercise> exercises = new ArrayList<>(List.of(
                    defaultExercise().name("match1").user(myUser).build(),
                    defaultExercise().name("match2").user(myUser).build(),
                    defaultExercise().name("match3").user(myUser).build()));

            Pageable pageable = PageRequest.of(0, 3);
            Page<Exercise> exercisesPage = new PageImpl<>(exercises, pageable, 3);

            when(exerciseRepository.fetchShallowByUserAndText(USER_ID, "match", pageable))
                    .thenReturn(exercisesPage);

            // Act
            Page<ListedExerciseResponse> response = exerciseService.searchExercises(myUser, "match",
                    pageable);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(exercisesPage.map(exerciseMapper::toListedResponse));
        }

    }
}