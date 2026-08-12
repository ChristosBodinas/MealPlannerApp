package org.example.mealplannerapp.dto.plan.response;

public record PlanGoalsResponse(
    double targetCalories,
    double targetProtein,
    double targetCarbs,
    double targetFat,
    double targetFiber
) {
}
