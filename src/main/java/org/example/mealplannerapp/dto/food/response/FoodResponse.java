package org.example.mealplannerapp.dto.food.response;

import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.Food;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Response DTO for displaying {@link Food} and associated
 * {@link VendorData} and {@link ReferenceUnit} data.
 */
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
        Set<VendorResponse> vendors
) {
}
