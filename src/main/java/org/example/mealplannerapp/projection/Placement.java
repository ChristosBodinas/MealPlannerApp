package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.common.Category;

/**
 * Interface projection for handling an entry's placement data.
 */
public interface Placement {
    Long getDayId();

    Category getCategory();

    int getPosition();
}