package org.example.mealplannerapp.projection.impl;

import org.example.mealplannerapp.projection.Goals;

public record GoalsImpl(
        double targetCalories,
        double targetProtein,
        double targetCarbs,
        double targetFat,
        double targetFiber
) implements Goals {

    @Override
    public double getTargetCalories() {
        return targetCalories();
    }

    @Override
    public double getTargetProtein() {
        return targetProtein();
    }

    @Override
    public double getTargetCarbs() {
        return targetCarbs();
    }

    @Override
    public double getTargetFat() {
        return targetFat();
    }

    @Override
    public double getTargetFiber() {
        return targetFiber();
    }


}
