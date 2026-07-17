package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.constants.Category;

public interface CategorySummary {
    Category getCategory();
    double getCalories();
    double getProtein();
    double getCarbs();
    double getFat();
    double getFiber();
}
