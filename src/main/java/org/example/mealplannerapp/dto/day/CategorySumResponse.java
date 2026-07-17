package org.example.mealplannerapp.dto.day;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.projection.CategorySummary;

public record CategorySumResponse(
    Category category,
    double calories,
    double protein,
    double carbs,
    double fat,
    double fiber
) {

    public static CategorySumResponse from(CategorySummary summary) {
        return new CategorySumResponse(
            summary.getCategory(),
            summary.getCalories(),
            summary.getProtein(),
            summary.getCarbs(),
            summary.getFat(),
            summary.getFiber());
    }
}
