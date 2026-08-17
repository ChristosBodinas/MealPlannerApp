package org.example.mealplannerapp.dto.food.response;

import java.math.BigDecimal;

public record PriceResponse(
    String vendorName,
    BigDecimal purchasePrice,
    BigDecimal purchaseGrams
) {
}
