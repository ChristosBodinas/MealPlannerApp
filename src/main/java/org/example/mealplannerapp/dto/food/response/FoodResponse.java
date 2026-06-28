package org.example.mealplannerapp.dto.food.response;

import java.util.Set;

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
        Set<FoodUnitResponse> units,
        Set<FoodPriceResponse> prices
) {
}
