package org.example.mealplannerapp.dto.food.response;

import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.Food;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link Food} data without the
 * associated {@link VendorData} and {@link ReferenceUnit} data.
 * Intended for listing multiple exercises.
 */
public record ListedFoodResponse(
        Long id,
        String name,
        String brand,
        BigDecimal calories100g,
        BigDecimal protein100g,
        BigDecimal carbs100g,
        BigDecimal fat100g,
        BigDecimal fiber100g,
        BigDecimal edibleRatio
) {
}
