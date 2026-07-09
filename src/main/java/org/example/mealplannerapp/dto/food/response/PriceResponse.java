package org.example.mealplannerapp.dto.food.response;

public record PriceResponse(
        String merchant,
        double purchasePrice,
        double purchaseGrams
) {
}
