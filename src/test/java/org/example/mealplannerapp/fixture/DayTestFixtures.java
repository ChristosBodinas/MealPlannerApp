package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.entity.Day;

import java.math.BigDecimal;

public class DayTestFixtures {

    private static final BigDecimal DEFAULT_TARGET_CALORIES = BigDecimal.valueOf(1500.0);
    private static final BigDecimal DEFAULT_TARGET_PROTEIN = BigDecimal.valueOf(100.0);
    private static final BigDecimal DEFAULT_TARGET_CARBS = BigDecimal.valueOf(200.0);
    private static final BigDecimal DEFAULT_TARGET_FAT = BigDecimal.valueOf(50.0);
    private static final BigDecimal DEFAULT_TARGET_FIBER = BigDecimal.valueOf(25.0);

    /**
     * Builds a {@link Day} entity fixture for testing.
     *
     * @return a Day builder with null {@code id}, {@code plan}, and {@code position}, and default
     * values in all other fields
     */
    public static Day.DayBuilder defaultDay() {
        return Day.builder()
                .targetCalories(DEFAULT_TARGET_CALORIES)
                .targetProtein(DEFAULT_TARGET_PROTEIN)
                .targetCarbs(DEFAULT_TARGET_CARBS)
                .targetFat(DEFAULT_TARGET_FAT)
                .targetFiber(DEFAULT_TARGET_FIBER);
    }

}