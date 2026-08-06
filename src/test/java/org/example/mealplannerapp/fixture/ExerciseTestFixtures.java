package org.example.mealplannerapp.fixture;

import java.util.HashSet;
import java.util.Set;

import org.example.mealplannerapp.embeddable.ExerciseLevel;
import org.example.mealplannerapp.entity.Exercise;

public class ExerciseTestFixtures {

    private static final String DEFAULT_NAME = "Running";
    private static final String DEFAULT_LEVEL_LOW = "Slow";
    private static final float DEFAULT_CALORIES_PER_MINUTE_LOW = 8.0;
    private static final String DEFAULT_LEVEL_HIGH = "Fast";
    private static final float DEFAULT_CALORIES_PER_MINUTE_HIGH = 20.0;

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
    
}
