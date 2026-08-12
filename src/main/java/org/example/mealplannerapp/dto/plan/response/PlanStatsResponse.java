package org.example.mealplannerapp.dto.plan.response;

public record PlanStatsResponse(
        double calories,
        double protein,
        double carbs,
        double fat,
        double fiber,
        double price
) {
}
