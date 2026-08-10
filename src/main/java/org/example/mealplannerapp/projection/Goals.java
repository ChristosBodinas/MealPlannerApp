package org.example.mealplannerapp.projection;

/**
 * Interface projection for handling a given plan or day's nutrition goals.
 * Goals
 */
public interface Goals {

    double getTargetCalories();

    double getTargetProtein();

    double getTargetCarbs();

    double getTargetFat();

    double getTargetFiber();
    
}
