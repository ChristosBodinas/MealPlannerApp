package org.example.mealplannerapp.service;

import java.util.List;
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
import org.example.mealplannerapp.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    private void verifyUniqueLevels(ExerciseRequest request) {
        Set<LevelRequest> levels = request.levels();

        if (levels != null && levels.size() > levels.stream().map(LevelRequest::name).distinct().count()) {
            throw new ServiceValidationException("An exercise can't have duplicate of the same intensity level.");
        }
    }

    public ExerciseResponse createExercise(User user, ExerciseRequest request) {
        verifyUniqueLevels(request);

        Exercise exercise = exerciseMapper.toExercise(request);
        exercise.setUser(user);

        Exercise saved = exerciseRepository.save(exercise);
        return exerciseMapper.toResponse(saved);
    }

    @Transactional
    public ExerciseResponse updateExercise(User user, Long exerciseId, ExerciseRequest request) {
        verifyUniqueLevels(request);

        Exercise exercise = exerciseRepository.fetchByIdVerified(user.getId(), exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested exercise (id: " + exerciseId + ") not found."));
        exerciseMapper.update(exercise, request);

        return exerciseMapper.toResponse(exercise);
    }

    @Transactional
    public void deleteExercise(User user, Long exerciseId) {
        if (exerciseRepository.deleteByIdVerified(user.getId(), exerciseId) == 0) {
            throw new ResourceNotFoundException("Requested exercise (id: " + exerciseId + ") not found.");
        }
    }

    public ExerciseResponse retrievExercise(User user, Long exerciseId) {
        Exercise exercise = exerciseRepository.fetchByIdVerified(user.getId(), exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested exercise (id: " + exerciseId + ") not found."));

        return exerciseMapper.toResponse(exercise);
    }

    public List<ListedExerciseResponse> searchExercises(User user, String search) {
        return exerciseRepository.fetchShallowByUserAndText(user.getId(), search)
            .stream()
            .map(exerciseMapper::toListedResponse)
            .toList();
    }
    
}