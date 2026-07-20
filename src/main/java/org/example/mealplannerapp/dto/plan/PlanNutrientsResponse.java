package org.example.mealplannerapp.dto.plan;

public record PlanNutrientsResponse(
    double calories,
    double protein,
    double carbs,
    double fat,
    double fiber
) {
}
