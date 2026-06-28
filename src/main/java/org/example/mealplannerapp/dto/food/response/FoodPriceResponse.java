package org.example.mealplannerapp.dto.food.response;

public record FoodPriceResponse(
        String seller,
        double purchasePrice,
        double purchaseGrams
) {
}
