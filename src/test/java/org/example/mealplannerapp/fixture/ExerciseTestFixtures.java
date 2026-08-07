package org.example.mealplannerapp.fixture;

import java.util.HashSet;
import java.util.Set;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.LevelResponse;
import org.example.mealplannerapp.embeddable.ExerciseLevel;
import org.example.mealplannerapp.entity.Exercise;

public class ExerciseTestFixtures {

    private static final String DEFAULT_NAME = "Running";
    private static final String DEFAULT_LEVEL_LOW = "Slow";
    private static final double DEFAULT_CALORIES_PER_MINUTE_LOW = 8.0;
    private static final String DEFAULT_LEVEL_HIGH = "Fast";
    private static final double DEFAULT_CALORIES_PER_MINUTE_HIGH = 20.0;

    /**
     * Method for building {@link Exercise} entities.
     * @return an Exercise builder with {@code name} and {@code levels} filled out,
     * but {@code id} and {@code user} unset
     */
    public static Exercise.ExerciseBuilder defaultExerciseBuilder() {
        return Exercise.builder()
            .name(DEFAULT_NAME)
            .levels(new HashSet<>(Set.of(
                new ExerciseLevel(DEFAULT_LEVEL_LOW, DEFAULT_CALORIES_PER_MINUTE_LOW),
                new ExerciseLevel(DEFAULT_LEVEL_HIGH, DEFAULT_CALORIES_PER_MINUTE_HIGH)
            )));
    }

    public static ExerciseRequest.ExerciseRequestBuilder defaultExerciseRequestBuilder() {
        return ExerciseRequest.builder()
                .levels(new HashSet<>(Set.of(
                new LevelRequest(DEFAULT_LEVEL_LOW, DEFAULT_CALORIES_PER_MINUTE_LOW),
                new LevelRequest(DEFAULT_LEVEL_HIGH, DEFAULT_CALORIES_PER_MINUTE_HIGH)
            )));
    }

    public static ExerciseResponse.ExerciseResponseBuilder defaultExerciseResponseBuilder() {
        return ExerciseResponse.builder()
            .name(DEFAULT_NAME)
            .levels(new HashSet<>(Set.of(
            new LevelResponse(DEFAULT_LEVEL_LOW, DEFAULT_CALORIES_PER_MINUTE_LOW),
            new LevelResponse(DEFAULT_LEVEL_HIGH, DEFAULT_CALORIES_PER_MINUTE_HIGH)
        )));
    }
    
}
