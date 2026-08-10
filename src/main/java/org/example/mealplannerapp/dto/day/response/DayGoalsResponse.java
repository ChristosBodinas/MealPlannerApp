package org.example.mealplannerapp.dto.day.response;

public record DayGoalsResponse(
    double targetCalories,
    double targetProtein,
    double targetCarbs,
    double targetFat,
    double targetFiber
) {
}
