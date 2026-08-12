package org.example.mealplannerapp.projection.impl;

import org.example.mealplannerapp.projection.Stats;

public record StatsImpl(
        double calories,
        double protein,
        double carbs,
        double fat,
        double fiber,
        double price
) implements Stats {

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
