package org.example.mealplannerapp.dto.day;

import jakarta.validation.constraints.Positive;

public record DayGoalsRequest(

    @Positive(message = "Daily calorie goal must be a positive number.")
    double calorieGoal,

    @Positive(message = "Daily protein goal must be a positive number.")
    double proteinGoal,

    @Positive(message = "Daily carbs goal must be a positive number.")
    double carbsGoal,

    @Positive(message = "Daily fat goal must be a positive number.")
    double fatGoal,

    @Positive(message = "Daily fiber goal must be a positive number.")
    double fiberGoal
) {
}
