package org.example.mealplannerapp.dto;

/**
 * Response DTO for displaying nutrition goals for a given day or plan.
 */
public record GoalsResponse(
        double targetCalories,
        double targetProtein,
        double targetCarbs,
        double targetFat,
        double targetFiber
) {
}
