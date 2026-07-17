package org.example.mealplannerapp.dto.day;

import java.util.List;

import org.example.mealplannerapp.projection.CategorySummary;

public record DaySumResponse(
    double calories,
    double protein,
    double carbs,
    double fat,
    double fiber,
    List<CategorySumResponse> categorySums
) {

    public static DaySumResponse from(
        double totalCalories, double totalProtein, double totalCarbs, double totalFat, double totalFiber,
        List<CategorySummary> summaries
    ) {
        return new DaySumResponse(totalCalories, totalProtein, totalCarbs, totalFat, totalFiber,
            summaries.stream().map(CategorySumResponse::from).toList());
    }
}
