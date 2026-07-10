package org.example.mealplannerapp.dto.entry.response;

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