package org.example.mealplannerapp.dto.food.response;

import org.example.mealplannerapp.embeddable.VendorData;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link VendorData} data.
 */
public record VendorResponse(
        String name,
        BigDecimal purchasePrice,
        BigDecimal purchaseGrams
) {
}
