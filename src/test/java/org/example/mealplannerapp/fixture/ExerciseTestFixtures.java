package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.embeddable.ExerciseLevel;
import org.example.mealplannerapp.entity.Exercise;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public final class ExerciseTestFixtures {

    private static final String DEFAULT_NAME = "Jogging";
    private static final String DEFAULT_LEVEL_DESC_1 = "Slow";
    private static final String DEFAULT_LEVEL_DESC_2 = "Fast";
    private static final BigDecimal DEFAULT_CALORIES_PER_MINUTE_1 = new BigDecimal("5.0");
    private static final BigDecimal DEFAULT_CALORIES_PER_MINUTE_2 = new BigDecimal("10.0");

    public static Exercise.ExerciseBuilder defaultExercise() {
        return Exercise.builder()
                .name(DEFAULT_NAME)
                .levels(new HashSet<>(Set.of(
                        new ExerciseLevel(DEFAULT_LEVEL_DESC_1, DEFAULT_CALORIES_PER_MINUTE_1),
                        new ExerciseLevel(DEFAULT_LEVEL_DESC_2, DEFAULT_CALORIES_PER_MINUTE_2)
                )));
    }

    public static ExerciseRequest.ExerciseRequestBuilder defauExerciseRequest() {
            return ExerciseRequest.builder()
                .name(DEFAULT_NAME)
                .levels(new HashSet<>(Set.of(
                    new LevelRequest(DEFAULT_LEVEL_DESC_1, DEFAULT_CALORIES_PER_MINUTE_1),
                    new LevelRequest(DEFAULT_LEVEL_DESC_2, DEFAULT_CALORIES_PER_MINUTE_2)
                )));
    }

}
