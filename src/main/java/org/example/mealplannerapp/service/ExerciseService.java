package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.DuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.ExerciseMapper;
import org.example.mealplannerapp.repository.ExerciseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * A service responsible for handling create, read, update, delete,
 * and text search operations on {@link Exercise} entities.
 */
@Service
@AllArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    private void throwIfDuplicateLevelNames(ExerciseRequest request) {
        Set<LevelRequest> levels = Optional.ofNullable(request.levels())
                .orElse(Collections.emptySet());

        if (levels.size() > levels.stream().map(LevelRequest::name).distinct().count()) {
            throw new DuplicateValueException("Cannot have multiple effort levels with the same name.");
        }
    }

    /**
     * Creates a new {@link Exercise} using the submitted {@code request} data and owned by {@code user},
     * then saves it to the database.
     *
     * @param user    the user making the request
     * @param request the submitted exercise data
     * @return the full data of the new exercise, including its effort levels
     * @throws DuplicateValueException if {@code request} contains multiple effort levels with identical names
     */
    public ExerciseResponse createExercise(
            User user, ExerciseRequest request
    ) {
        throwIfDuplicateLevelNames(request);

        Exercise exercise = exerciseMapper.toExercise(request);
        exercise.setUser(user);

        Exercise saved = exerciseRepository.save(exercise);
        return exerciseMapper.toResponse(saved);
    }

    /**
     * Updates the {@link Exercise} identified by {@code exerciseId} and owned by {@code user} with the
     * submitted {@code request} data.
     *
     * @param user       the user making the request
     * @param exerciseId the identifier of the exercise to be updated
     * @param request    the submitted exercise data
     * @return the full data of the updated exercise, including its effort levels
     * @throws DuplicateValueException   if {@code request} contains multiple effort levels with identical names
     * @throws ResourceNotFoundException if {@code exerciseId} does not correspond to an existing
     *                                   exercise owned by {@code user}
     */
    @Transactional
    public ExerciseResponse updateExercise(
            User user, Long exerciseId, ExerciseRequest request
    ) {
        throwIfDuplicateLevelNames(request);

        Long userId = user.getId();
        Exercise exercise = exerciseRepository.fetchByIdVerified(userId, exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested exercise (id: " + exerciseId + ") not found."));

        exerciseMapper.update(exercise, request);
        return exerciseMapper.toResponse(exercise);
    }

    /**
     * Deletes the {@link Exercise} identified by {@code exerciseId} and owned by {@code user}, along
     * with its associated effort levels.
     *
     * @param user       the user making the request
     * @param exerciseId the identifier of the exercise to be deleted
     * @throws ResourceNotFoundException if {@code exerciseId} does not correspond to an existing
     *                                   exercise owned by {@code user}
     */
    @Transactional
    public void deleteExercise(
            User user, Long exerciseId
    ) {
        Long userId = user.getId();
        if (exerciseRepository.deleteByIdVerified(userId, exerciseId) == 0) {
            throw new ResourceNotFoundException(
                    "Requested exercise (id: " + exerciseId + ") not found.");
        }
    }

    /**
     * Retrieves the {@link Exercise} identified by {@code exerciseId} and owned by {@code user}, along
     * with its associated effort levels.
     *
     * @param user       the user making the request
     * @param exerciseId the identifier of the exercise to be retrieved
     * @return the full data of the requested exercise, including its effort levels
     * @throws ResourceNotFoundException if {@code exerciseId} does not correspond to an existing
     *                                   exercise owned by {@code user}
     */
    public ExerciseResponse retrieveExercise(
            User user, Long exerciseId
    ) {
        Long userId = user.getId();
        Exercise exercise = exerciseRepository.fetchByIdVerified(userId, exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested exercise (id: " + exerciseId + ") not found."));

        return exerciseMapper.toResponse(exercise);
    }

    /**
     * Retrieves a page of {@link Exercise} entities owned by {@code user} whose names contain {@code searchText}.
     * If {@code searchText} is empty or {@code null}, exercises are retrieved regardless of their names.
     * Text matching is case-insensitive.
     * <p>Results are paginated and sorted according to {@code pageable}.</p>
     *
     * @param user       the user making the request
     * @param searchText the text to match against exercise names
     * @param pageable   the pagination and sorting parameters to apply to the results
     * @return a page containing the top-level data of the retrieved exercises,
     * excluding their associated effort levels
     */
    public Page<ListedExerciseResponse> searchExercises(
            User user, String searchText, Pageable pageable
    ) {
        Long userId = user.getId();

        return exerciseRepository.fetchShallowByUserAndText(userId, searchText, pageable)
                .map(exerciseMapper::toListedResponse);
    }

}
