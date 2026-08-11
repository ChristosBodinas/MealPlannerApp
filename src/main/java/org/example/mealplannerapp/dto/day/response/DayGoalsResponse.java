package org.example.mealplannerapp.dto.day.response;

/**
 * Response DTO for displaying a day's nutritional target.
 */
public record DayGoalsResponse(
    double targetCalories,
    double targetProtein,
    double targetCarbs,
    double targetFat,
    double targetFiber
) {
}
