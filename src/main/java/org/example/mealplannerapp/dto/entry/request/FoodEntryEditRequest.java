package org.example.mealplannerapp.dto.entry.request;

import lombok.Builder;

@Builder
public record FoodEntryEditRequest(
    double grams,
    String displayUnit,
    String displayMerchant
) implements EntryEditRequest {
}
