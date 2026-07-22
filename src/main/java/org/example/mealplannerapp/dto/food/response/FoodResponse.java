package org.example.mealplannerapp.dto.food.response;

import lombok.Builder;

import java.util.Set;

/**
 * Response DTO for displaying foods, along with their units and prices.
 */
@Builder
public record FoodResponse(
        Long id,
        String name,
        String brand,
        double caloriesPer100g,
        double proteinPer100g,
        double carbsPer100g,
        double fatPer100g,
        double fiberPer100g,
        double edibleRatio,
        Set<UnitResponse> units,
        Set<PriceResponse> prices
) {
}
