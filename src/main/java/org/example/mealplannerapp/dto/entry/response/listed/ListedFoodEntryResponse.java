package org.example.mealplannerapp.dto.entry.response.listed;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.entry.FoodEntry;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link FoodEntry} data without
 * the referenced {@link Food} data.
 */
public record ListedFoodEntryResponse(
        Long id,
        Category category,
        int position,
        String name,
        BigDecimal grams,
        BigDecimal unitQuantity,
        String unitName,
        String vendorName,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) implements ListedEntryResponse {
}
