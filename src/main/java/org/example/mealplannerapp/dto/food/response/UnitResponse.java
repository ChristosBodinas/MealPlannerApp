package org.example.mealplannerapp.dto.food.response;

import java.math.BigDecimal;

public record UnitResponse(
    String name,
    BigDecimal grams
) {
}
