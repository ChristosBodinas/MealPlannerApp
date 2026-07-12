package org.example.mealplannerapp.dto.entry.request.create;

import lombok.Builder;
import org.example.mealplannerapp.constants.Category;

@Builder
public record FoodEntryCreateRequest(
    Category category,
    Long foodId,
    double grams,
    String displayUnit,
    String displayMerchant
) implements EntryCreateRequest {
}
