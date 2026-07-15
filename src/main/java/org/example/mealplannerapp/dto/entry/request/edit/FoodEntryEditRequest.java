package org.example.mealplannerapp.dto.entry.request.edit;

public record FoodEntryEditRequest(
    double grams,
    String displayUnit,
    String displayMerchant
) implements EntryEditRequest {
}
