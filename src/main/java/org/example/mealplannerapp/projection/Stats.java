package org.example.mealplannerapp.projection;

/**
 * Interface for fetching nutrition and price values in one go.
 */
public interface Stats {

    double getCalories();

    double getProtein();

    double getCarbs();

    double getFat();

    double getFiber();

    double getPrice();
}
