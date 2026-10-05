package org.example.mealplannerapp.dto.plan.response;

import java.math.BigDecimal;

public record ShopItemResponse(
        String name,
        BigDecimal grams
) {
}
