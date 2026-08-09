package org.example.mealplannerapp.service;

import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.ExerciseMapper;
import org.example.mealplannerapp.mapper.ExerciseMapperImpl;
import org.example.mealplannerapp.repository.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceUnitTests {

    // MOCK, SPIES, CAPTORS
    @Mock
    private ExerciseRepository exerciseRepository;
    @Captor
    private ArgumentCaptor<Exercise> exerciseCaptor;
    
    // VARIABLES
    private ExerciseMapper exerciseMapper;
    private ExerciseService exerciseService;
    private User myUser;

    // CONSTANTS
    private static final long USER_ID = 1L;
    private static final long EXERCISE_ID = 44L;

    // HELPER METHODS
    private ExerciseRequest requestWithDuplicateLevels() {
        return ExerciseRequest.builder()
            .name("Unimportant")
            .levels(new HashSet<>(Set.of(
                new LevelRequest("Slow", 5.7),
                new LevelRequest("Slow", 8.6))))
            .build();
    }

    // BEFORE EACH
    @BeforeEach
    void prepareAllTests() {
        myUser = defaultUserBuilder().id(USER_ID).build();

        exerciseMapper = new ExerciseMapperImpl();
        exerciseService = new ExerciseService(exerciseRepository, exerciseMapper);
    }

    // TESTS PROPER
    @Nested
    @DisplayName("createExercise")
    class CreateExercise {

        @Test
        @DisplayName("Creates a new exercise and saves it to the database when the input data is valid.")
        void exerciseCreated() {
            // Arrange
            ExerciseRequest request = defaultExerciseRequestBuilder().build();
            Exercise saved = defaultExerciseBuilder().id(EXERCISE_ID).build();

            when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);

            // Act
            ExerciseResponse result = exerciseService.createExercise(myUser, request);

            // Assert
            assertThat(result).isEqualTo(exerciseMapper.toResponse(saved));

            verify(exerciseRepository).save(exerciseCaptor.capture());
            assertThat(exerciseCaptor.getValue().getUser()).isEqualTo(myUser);
            assertThat(exerciseCaptor.getValue())
                    .usingRecursiveComparison()
                    .ignoringFields("id", "user")
                    .isEqualTo(exerciseMapper.toExercise(request));
        }

        @Test
        @DisplayName("Throws ServiceValidationErrorException when the input data contains duplicate intensity level names.")
        void duplicateLevels() {
            // Arrange
            ExerciseRequest request = requestWithDuplicateLevels();

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.createExercise(myUser, request))
                    .isInstanceOf(ServiceValidationException.class);
            verify(exerciseRepository, never()).save(any(Exercise.class));
        }

    }

    @Nested
    @DisplayName("updateExercise")
    class UpdateExercise {

        private static final String NAME_BEFORE = "Running";
        private static final String NAME_AFTER = "Flying";

        @Test
        @DisplayName("Updates the requested exercise when it exists and belongs to the given user and the input data is valid.")
        void exerciseUpdated() {
            // Arrange
            ExerciseRequest request = defaultExerciseRequestBuilder().name(NAME_AFTER).build();
            Exercise found = defaultExerciseBuilder().id(EXERCISE_ID).name(NAME_BEFORE).build();

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.of(found));

            // Act
            ExerciseResponse result = exerciseService.updateExercise(myUser, EXERCISE_ID, request);

            // Assert
            assertThat(result).isEqualTo(exerciseMapper.toResponse(found));
            assertThat(found.getName()).isEqualTo(NAME_AFTER);
        }

        @Test
        @DisplayName("Throws a ServiceValidationException when the input data contains duplicate intensity level names.")
        void duplicateLevels() {
            // Arrange
            ExerciseRequest request = requestWithDuplicateLevels();
            ExerciseMapper spyMapper = spy(exerciseMapper);

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.updateExercise(myUser, EXERCISE_ID, request))
                .isInstanceOf(ServiceValidationException.class);
            verifyNoInteractions(spyMapper);

        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested exercise does not exist or belongs to a different user.")
        void exerciseNotFound() {
            // Arrange
            ExerciseRequest request = defaultExerciseRequestBuilder().build();

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.empty());

            // Act + ASsert
            assertThatThrownBy(() -> exerciseService.updateExercise(myUser, EXERCISE_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("deleteExercise")
    class DeleteExercise {

        @Test
        @DisplayName("Deletes the requested exercise when it exists and belongs to the given user.")
        void exerciseDeleted() {
            // Arrange
            when(exerciseRepository.deleteByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(1);

            // Act
            assertThatCode(() -> exerciseService.deleteExercise(myUser, EXERCISE_ID))
                .doesNotThrowAnyException();
            
            // Assert
            verify(exerciseRepository).deleteByIdVerified(USER_ID, EXERCISE_ID);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested exercise does not exist or belongs to another user.")
        void exerciseNotFound() {
            // Arrange
            when(exerciseRepository.deleteByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(0);

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.deleteExercise(myUser, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("retrieveExercise")
    class RetrieveExercise {

        @Test
        @DisplayName("Returns the requested exercise's full data when it exists and belongs to the given user.")
        void exerciseRetrieved() {
            // Arrange
            Exercise found = defaultExerciseBuilder().id(EXERCISE_ID).build();

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.of(found));

            // Act
            ExerciseResponse result = exerciseService.retrievExercise(myUser, EXERCISE_ID);

            // Assert
            assertThat(result).isEqualTo(exerciseMapper.toResponse(found));
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested exercise does not exist or belongs to a different user.")
        void exerciseNotFound() {
            // Arrange
            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.retrievExercise(myUser, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("searchExercises")
    class SearchExercises {

        List<Exercise> listedExercises(int number) {
            List<Exercise> exercises = new ArrayList<>(number);

            for (int i = 1; i <= number; i++) {
                Exercise exercise = defaultExerciseBuilder()
                    .id((long) i)
                    .user(myUser)
                    .name("Listed Exercise #" + i)
                    .build();
                exercises.add(exercise);
            }

            return exercises;
        }

        @Test
        @DisplayName("Returns a list of matching exercises when given a user and a search string.")
        void exercisesRetrieved() {
            // Arrange
            List<Exercise> exercises = listedExercises(5);

            when(exerciseRepository.fetchShallowByUserAndText(USER_ID, "text")).thenReturn(exercises);

            // Act
            List<ListedExerciseResponse> result = exerciseService.searchExercises(myUser, "text");

            // Assert
            assertThat(result).isEqualTo(exercises.stream().map(exerciseMapper::toListedResponse).toList());
        }

    }

}
