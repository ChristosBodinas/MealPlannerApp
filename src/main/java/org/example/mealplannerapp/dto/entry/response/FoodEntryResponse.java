package org.example.mealplannerapp.dto.entry.response;

import lombok.Builder;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.food.response.FoodResponse;

/**
 * Response DTO for displaying food entries, along with their reference food and its associated units/prices.
 */
@Builder
public record FoodEntryResponse(
        Long id,
        Category category,
        int position,
        FoodResponse foodResponse,
        double grams,
        String displayUnit,
        String selectedVendor
) implements EntryResponse {
}