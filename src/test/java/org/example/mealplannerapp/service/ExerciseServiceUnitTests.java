package org.example.mealplannerapp.service;

import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.embeddable.ExerciseLevel;
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
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceUnitTests {

    // CLASS CONSTANTS
    private static final long USER_ID = 1L;
    private static final long EXERCISE_ID = 99L;

    // CLASS FIELDS
    private ExerciseService exerciseService;
    private ExerciseMapper exerciseMapper;
    @Mock private ExerciseRepository exerciseRepository;
    @Captor private ArgumentCaptor<Exercise> captor;

    private User myUser;

    // HELPER METHODS
    private ExerciseRequest prepareInvalidRequest() {        
        return defauExerciseRequest()
            .levels(new HashSet<>(Set.of(
                new LevelRequest("dupe", new BigDecimal("5.0")),
                new LevelRequest("dupe", new BigDecimal("7.0")))))
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
        @DisplayName("Given a valid ExerciseRequest, creates and saves a new Exercise owned by the current user, " +
            "and returns an ExerciseResponse.")
        void validData_createsExerciseAndReturnsResponse() {
            // Arrange
            ExerciseRequest request = defauExerciseRequest().build();
            Exercise saved = defaultExercise().build();

            when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);

            // Act
            ExerciseResponse response = exerciseService.createExercise(myUser, request);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                .isEqualTo(exerciseMapper.toResponse(saved));

            verify(exerciseRepository).save(captor.capture());
            Exercise created = captor.getValue();

            assertThat(created).as("Created exercise fields should match request data.")
                .usingRecursiveComparison()
                .ignoringFields("id", "user")
                .isEqualTo(request);
        }

        @Test
        @DisplayName("Given an ExerciseRequest with duplicate level names, throws a DuplicateValueException.")
        void duplicateLevelNames_throwsDuplicateValue() {
            // Arrange
            ExerciseRequest request = prepareInvalidRequest();

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.createExercise(myUser, request))
                .as("Method should throw a DuplicateValueException.")
                .isInstanceOf(ResourceNotFoundException.class);

            verify(exerciseRepository, never().description("Nothing should be saved to the database."))
                .save(any(Exercise.class));
        }

    }

    @Nested
    @DisplayName("updateExercise")
    class UpdateExercise {

        private ExerciseRequest prepareValidRequest(Exercise exercise) {
            Set<LevelRequest> levelRequests = new HashSet<>(exercise.getLevels().size());

            for (ExerciseLevel level : exercise.getLevels()) {
                levelRequests.add(new LevelRequest(
                    level.getIntensityDesc() + "_edited",
                    level.getCaloriesPerMinute().add(new BigDecimal("15.0"))
                ));
            }

            return ExerciseRequest.builder()
                .name(exercise.getName() + "_edited")
                .levels(levelRequests)
                .build();
        }

        @Test
        @DisplayName("Given a valid exerciseId owned by the current user and a valid ExerciseRequest, " +
            "updates the requested exercise and returns an ExerciseResponse.")
        void validIdAndData_updatesExerciseAndReturnsResponse() {
            // Arrange
            Exercise fetched = defaultExercise().id(EXERCISE_ID).user(myUser).build();
            ExerciseRequest request = prepareValidRequest(fetched);

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.of(fetched));

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
        @DisplayName("Given an invalid or non-owned exerciseId, throws a ResourceNotFoundException.")
        void invalidId_throwsResourceNotFound() {
            // Arrange
            ExerciseRequest request = defauExerciseRequest().build();

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.updateExercise(myUser, EXERCISE_ID, request))
                .as("Method should throw a ResourceNotFoundException.")
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Given an ExerciseRequest with duplicate level names, throw a DuplicateValueException.")
        void duplicateLevelNames_throwsDuplicateValue() {
            // Arrange
            ExerciseRequest request = prepareInvalidRequest();

            // Act + Assert
            assertThatThrownBy(() -> exerciseService.updateExercise(myUser, EXERCISE_ID, request))
                .as("Method should throw a DuplicateValueException.")
                .isInstanceOf(DuplicateValueException.class);
        }

    }

    @Nested
    @DisplayName("deleteExercise")
    class DeleteExercise {

        @Test
        @DisplayName("Given a valid exerciseID owned by the current user, deletes the requested Exercise.")
        void validId_deletesExercise() {
            // Arrange
            when(exerciseRepository.deleteByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(1);

            // Act + Assert
            assertThatCode(() -> exerciseService.deleteExercise(myUser, EXERCISE_ID))
                .as("Method should complete without throwing any exceptions.")
                .doesNotThrowAnyException();

            verify(exerciseRepository).deleteByIdVerified(USER_ID, EXERCISE_ID);
        }

        @Test
        @DisplayName("Given an invalid or non-owned exerciseId, throws a ResourceNotFoundException.")
        void invalidId_throwsResourceNotFound() {
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
        @DisplayName("Given a valid exerciseId owned by the current user, " +
            "returns the requested ExerciseResponse.")
        void validId_returnsExerciseResponse() {
            // Arrange
            Exercise fetched = defaultExercise().id(EXERCISE_ID).user(myUser).build();

            when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.of(fetched));

            // Act
            ExerciseResponse response = exerciseService.retrieveExercise(myUser, EXERCISE_ID);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                .isEqualTo(exerciseMapper.toResponse(fetched));
        }

        @Test
        @DisplayName("Given an invalid or non-owned exerciseId, throws a ResourceNotFoundException.")
        void invalidId_throwsResourceNotFound() {
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
        @DisplayName("Returns a List of ListedExerciseResponse DTOs.")  // TODO: Rephrase?
        void returnsMatchingExercises() {
            // Arrange
            List<Exercise> exercises = new ArrayList<>(List.of(
                defaultExercise().name("match1").user(myUser).build(),
                defaultExercise().name("match2").user(myUser).build(),
                defaultExercise().name("match3").user(myUser).build()
            ));

            Pageable pageable = PageRequest.of(0, 3);
            Page<Exercise> exercisesPage = new PageImpl<>(exercises, pageable, 3);

            when(exerciseRepository.fetchShallowByUserAndText(USER_ID, "match", pageable)).thenReturn(exercisesPage);

            // Act
            Page<ListedExerciseResponse> response = exerciseService.searchExercises(myUser, "match", pageable);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                .isEqualTo(exercisesPage.map(exerciseMapper::toListedResponse));
        }

    }
    
}
