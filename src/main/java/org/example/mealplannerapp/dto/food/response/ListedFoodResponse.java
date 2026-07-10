package org.example.mealplannerapp.dto.food.response;

import lombok.Builder;

@Builder
public record ListedFoodResponse(
        Long id,
        String name,
        String brand,
        double caloriesPer100g,
        double proteinPer100g,
        double carbsPer100g,
        double fatPer100g,
        double fiberPer100g
) {
}