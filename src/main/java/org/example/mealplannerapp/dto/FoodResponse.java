package org.example.mealplannerapp.dto;

public record FoodResponse(
        Long id,
        String name,
        String brand,
        double calories100g,
        double protein100g,
        double carbs100g,
        double fat100g,
        double fiber100g,
        double price100g
) {
}
