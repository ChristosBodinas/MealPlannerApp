package org.example.mealplannerapp.dto.food.response;

/**
 * Response DTO for displaying food prices.
 */
public record PriceResponse(
        String vendor,
        double purchasePrice,
        double purchaseGrams
) {
}