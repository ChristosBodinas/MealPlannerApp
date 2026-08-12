package org.example.mealplannerapp.dto.plan.response;

public record ShoppingItemResponse(
        String name,
        String brand,
        String vendor,
        double grams,
        double price
) {
}
