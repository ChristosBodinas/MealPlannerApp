package org.example.mealplannerapp.projection;

import java.math.BigDecimal;

public record ShopItem(
        String name,
        BigDecimal grams
) {
}
