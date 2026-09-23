package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Projection used for fetching {@link Entry} placement data from the database.
 */
public record Placement(
        Long dayId,
        Category category,
        int position
) {
}
