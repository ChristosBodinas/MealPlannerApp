package org.example.mealplannerapp.dto.food.response;

public record FoodPriceResponse(
        String merchant,
        double purchasePrice,
        double purchaseGrams
) {
}
