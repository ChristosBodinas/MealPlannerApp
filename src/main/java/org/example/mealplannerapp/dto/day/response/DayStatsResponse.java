package org.example.mealplannerapp.dto.day.response;

/**
 * Response DTO for displaying a day's total nutritional and price values.
 */
public record DayStatsResponse(
    double calories,
    double protein,
    double carbs,
    double fat,
    double fiber,
    double price
) {
}
