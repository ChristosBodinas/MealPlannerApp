package org.example.mealplannerapp.dto.day.request;

import jakarta.validation.constraints.Positive;

public record DayGoalsRequest(

    @Positive(message = "Daily calorie goal must be a positive number.")
    double targetCalories,
    
    @Positive(message = "Daily protein goal must be a positive number.")
    double targetProtein,

    @Positive(message = "Daily carbohydrate goal must be a positive number.")
    double targetCarbs,

    @Positive(message = "Daily fat goal must be a positive number.")
    double targetFat,

    @Positive(message = "Daily fiber goal must be a positive number.")
    double targetFiber
) {
}
