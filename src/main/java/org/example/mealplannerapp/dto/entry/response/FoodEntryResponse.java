package org.example.mealplannerapp.dto.entry.response;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.entry.FoodEntry;

import java.math.BigDecimal;

/**
 * Response DTO interface for displaying {@link FoodEntry} data.
 */
public record FoodEntryResponse(
        Long id,
        String name,
        Category category,
        int position,
        FoodResponse food,
        BigDecimal grams,
        String unitName,
        String vendorName,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) implements EntryResponse {
}
