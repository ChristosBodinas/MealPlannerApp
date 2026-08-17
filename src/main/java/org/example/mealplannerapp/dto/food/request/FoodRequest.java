package org.example.mealplannerapp.dto.food.request;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.Valid;

public record FoodRequest(
    // TODO: Validation.
    String name,
    String brand,
    BigDecimal calories100g,
    BigDecimal protein100g,
    BigDecimal carbs100g,
    BigDecimal fat100g,
    BigDecimal fiber100g,
    BigDecimal edibleRatio,
    Set<@Valid UnitRequest> units,
    Set<@Valid PriceRequest> prices
) {
}
