package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.entity.Day;

public class DayTestFixtures {

    private static final int DEFAULT_POSITION = 1;
    private static final double DEFAULT_TARGET_CALORIES = 2000.0;
    private static final double DEFAULT_TARGET_PROTEIN = 120.0;
    private static final double DEFAULT_TARGET_CARBS = 250.0;
    private static final double DEFAULT_TARGET_FAT = 60.0;
    private static final double DEFAULT_TARGET_FIBER = 30.0;

    /**
     * Method for building {@link Day} entities.
     *
     * @return a Day builder with all fields except {@code id} and {@code plan} filled out
     */
    public static Day.DayBuilder defaultDayBuilder() {
        return Day.builder()
                .position(DEFAULT_POSITION)
                .targetCalories(DEFAULT_TARGET_CALORIES)
                .targetProtein(DEFAULT_TARGET_PROTEIN)
                .targetCarbs(DEFAULT_TARGET_CARBS)
                .targetFat(DEFAULT_TARGET_FAT)
                .targetFiber(DEFAULT_TARGET_FIBER);
    }

}
