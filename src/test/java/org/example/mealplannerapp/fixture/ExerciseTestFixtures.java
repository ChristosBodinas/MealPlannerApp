package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.entity.Exercise;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class ExerciseTestFixtures {

    private static final String DEFAULT_NAME = "Jogging";

    private static final String DEFAULT_LEVEL_NAME_1 = "Slow";
    private static final BigDecimal DEFAULT_BURN_RATE_1 = new BigDecimal("5.0");

    private static final String DEFAULT_LEVEL_NAME_2 = "Fast";
    private static final BigDecimal DEFAULT_BURN_RATE_2 = new BigDecimal("10.0");

    /**
     * Builds an {@link Exercise} entity fixture for testing.
     *
     * @return an Exercise builder with null {@code id} and {@code user},
     * and default values in all other fields
     */
    public static Exercise.ExerciseBuilder defaultExercise() {
        Set<EffortLevel> defaultLevels = new HashSet<>(Set.of(
                new EffortLevel(DEFAULT_LEVEL_NAME_1, DEFAULT_BURN_RATE_1),
                new EffortLevel(DEFAULT_LEVEL_NAME_2, DEFAULT_BURN_RATE_2)
        ));

        return Exercise.builder()
                .name(DEFAULT_NAME)
                .levels(defaultLevels);
    }

}
