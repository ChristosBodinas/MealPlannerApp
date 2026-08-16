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

@Service
@AllArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    private void verifyUniqueLevels(ExerciseRequest request) {
        if (request.levels() != null && request.levels().size() >
        request.levels().stream().map(LevelRequest::intensityDesc).distinct().count()) {
            throw new DuplicateValueException("text");
        }
    }

    public ExerciseResponse createExercise(
        User user, ExerciseRequest request
    ) {
        verifyUniqueLevels(request);
        
        Exercise created = exerciseMapper.toExercise(request);
        created.setUser(user);

        Exercise saved = exerciseRepository.save(created);
        return exerciseMapper.toResponse(saved);
    }

    @Transactional
    public ExerciseResponse updateExercise(
        User user, Long exerciseId, ExerciseRequest request
    ) {
        verifyUniqueLevels(request);

        Long userId = user.getId();
        Exercise fetched = exerciseRepository.fetchByIdVerified(userId, exerciseId).orElseThrow(
            () -> new ResourceNotFoundException("Requested exercise (id: " + exerciseId + ") not found."));

        exerciseMapper.update(fetched, request);
        return exerciseMapper.toResponse(fetched);
    }

    @Transactional
    public void deleteExercise(
        User user, Long exerciseId
    ) {
        Long userId = user.getId();
        if (exerciseRepository.deleteByIdVerified(userId, exerciseId) == 0) {
            throw new ResourceNotFoundException("Requested exercise (id: " + exerciseId + ") not found.");
        }
    }

    public ExerciseResponse retrieveExercise(
        User user, Long exerciseId
    ) {
        Long userId = user.getId();
        Exercise fetched = exerciseRepository.fetchByIdVerified(userId, exerciseId).orElseThrow(
            () -> new ResourceNotFoundException("Requested exercise (id: " + exerciseId + ") not found."));

        return exerciseMapper.toResponse(fetched);
    }

    public Page<ListedExerciseResponse> searchExercises(
        User user, String text, Pageable pageable
    ) {
        Long userId = user.getId();
        return exerciseRepository.fetchShallowByUserAndText(userId, text, pageable)
            .map(exerciseMapper::toListedResponse);
    }
    
}
