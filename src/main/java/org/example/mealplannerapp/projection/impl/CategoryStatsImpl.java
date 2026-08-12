package org.example.mealplannerapp.projection.impl;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.projection.CategoryStats;

public record CategoryStatsImpl(
        Category category,
        double calories,
        double protein,
        double carbs,
        double fat,
        double fiber,
        double price
) implements CategoryStats {
    @Override
    public Category getCategory() {
        return category();
    }

    @Override
    public double getCalories() {
        return calories();
    }

    @Override
    public double getProtein() {
        return protein();
    }

    @Override
    public double getCarbs() {
        return carbs();
    }

    @Override
    public double getFat() {
        return fat();
    }

    @Override
    public double getFiber() {
        return fiber();
    }

    @Override
    public double getPrice() {
        return price();
    }

}
