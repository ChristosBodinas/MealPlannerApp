package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Projection for retrieving an {@link Entry}'s day ID,
 * category and position from the database.
 */
public record Placement(
        Long dayId,
        Category category,
        int position
) {
}
