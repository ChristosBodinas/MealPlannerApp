package org.example.mealplannerapp.dto.entry.request;

import lombok.Builder;
import org.example.mealplannerapp.constants.Category;

@Builder
public record FoodEntryCreateRequest(
    Category category,
    int position,
    Long foodId,
    double grams,
    String displayUnit,
    String displayMerchant
) implements EntryCreateRequest {
}
