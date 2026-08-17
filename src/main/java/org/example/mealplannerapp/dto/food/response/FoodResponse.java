package org.example.mealplannerapp.dto.food.response;

import java.math.BigDecimal;
import java.util.Set;

public record FoodResponse(
    Long id,
    String name,
    String brand,
    BigDecimal calories100g,
    BigDecimal protein100g,
    BigDecimal carbs100g,
    BigDecimal fat100g,
    BigDecimal fiber100g,
    BigDecimal edibleRatio,
    Set<UnitResponse> units,
    Set<PriceResponse> prices
) {
}
