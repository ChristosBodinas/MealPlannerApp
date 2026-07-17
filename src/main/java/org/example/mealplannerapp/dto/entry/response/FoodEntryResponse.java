package org.example.mealplannerapp.dto.entry.response;

import lombok.Builder;
import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.food.response.FoodResponse;

@Builder
public record FoodEntryResponse(
        Long id,
        Category category,
        int position,
        FoodResponse foodResponse,
        double grams,
        String displayUnit,
        String displayMerchant
) implements EntryResponse {
}