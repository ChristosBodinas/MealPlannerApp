package org.example.mealplannerapp.dto.plan.response;

import java.util.Set;

import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.dto.day.response.DayResponse;

/**
 * Response DTO for displaying a plan's parameters and the ids and positions of its days.
 */
public record PlanResponse(
    Long id,
    String name,
    double startWeight,
    double targetLoss,
    ActivityLevel activityLevel,
    double targetCalories,
    double targetProtein,
    double targetCarbs,
    double targetFat,
    double targetFiber,
    Set<DayResponse> dayResponses
) {
}
