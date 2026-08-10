package org.example.mealplannerapp.projection;

/**
 * Interface projection for handling a given entry, category, day, or plan's nutrition and price values.
 * Stats
 */
public interface Stats {

    double getCalories();

    double getProtein();

    double getCarbs();

    double getFat();

    double getFiber();

    double getPrice();

}
