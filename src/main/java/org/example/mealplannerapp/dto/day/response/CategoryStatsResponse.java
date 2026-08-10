package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.common.Category;

// TODO: Javadocs for day DTOs.
public record CategoryStatsResponse(
    Category category,
    double calories,
    double protein,
    double carbs,
    double fat,
    double fiber,
    double price
) {
}