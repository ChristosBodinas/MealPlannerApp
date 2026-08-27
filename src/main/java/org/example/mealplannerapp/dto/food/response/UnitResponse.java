package org.example.mealplannerapp.dto.food.response;

import org.example.mealplannerapp.embeddable.ReferenceUnit;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link ReferenceUnit} data.
 */
public record UnitResponse(
        String name,
        BigDecimal grams
) {
}
