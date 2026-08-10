package org.example.mealplannerapp.dto.day.response;

public record DayStatsResponse(
    double calories,
    double protein,
    double carbs,
    double fat,
    double fiber,
    double price
) {
}
