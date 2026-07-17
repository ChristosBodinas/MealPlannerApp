package org.example.mealplannerapp.fixtures;

import org.example.mealplannerapp.dto.day.DayGoalsResponse;
import org.example.mealplannerapp.entity.Day;

public class DayTestFixtures {

    private static final Long DEFAULT_ID = 1L;
    private static final double DEFAULT_CALORIES_GOAL = 2000.0;
    private static final double DEFAULT_PROTEIN_GOAL = 100.0;
    private static final double DEFAULT_CARBS_GOAL = 250.0;
    private static final double DEFAULT_FAT_GOAL = 60.0;
    private static final double DEFAULT_FIBER_GOAL = 35.0;

    /**
     * Method for building {@link Day} entities.
     * @return a Day builder with default values for all fields except {@code plan}
     */
    public static Day.DayBuilder defaultDayBuilder() {
        return Day.builder()
            .id(DEFAULT_ID)
            .caloriesGoal(DEFAULT_CALORIES_GOAL)
            .proteinGoal(DEFAULT_PROTEIN_GOAL)
            .carbsGoal(DEFAULT_CARBS_GOAL)
            .fatGoal(DEFAULT_FAT_GOAL)
            .fiberGoal(DEFAULT_FIBER_GOAL);
    }

    /**
     * Method for building {@link DayGoalsResponse} DTOs.
     * @return a DayGoalsResponse builder with default values for all fields
     */
    public static DayGoalsResponse.DayGoalsResponseBuilder defaultDayGoalsResponseBuilder() {
        return DayGoalsResponse.builder()
            .caloriesGoal(DEFAULT_CALORIES_GOAL)
            .proteinGoal(DEFAULT_PROTEIN_GOAL)
            .carbsGoal(DEFAULT_CARBS_GOAL)
            .fatGoal(DEFAULT_FAT_GOAL)
            .fiberGoal(DEFAULT_FIBER_GOAL);
    }
    
}
