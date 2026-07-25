package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.constants.Category;

/**
 * Interface for fetching
 */
public interface Placement {
    Long getDayId();

    Category getCategory();

    int getPosition();
}
