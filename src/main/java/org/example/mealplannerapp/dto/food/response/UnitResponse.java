package org.example.mealplannerapp.dto.food.response;

/**
 * Response DTO for displaying food units.
 */
public record UnitResponse(
        String name,
        double grams
) {
}