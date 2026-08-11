package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.common.Category;

/**
 * Response DTO for displaying the total nutrition and price values for a given category.
 */
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