package org.example.mealplannerapp.dto.entry.response;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.entry.FoodEntry;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link FoodEntry} data along
 * with the referenced {@link Food} data.
 */
public record FoodEntryResponse(
        Long id,
        Category category,
        int position,
        String name,
        FoodResponse food,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) implements EntryResponse {
}
