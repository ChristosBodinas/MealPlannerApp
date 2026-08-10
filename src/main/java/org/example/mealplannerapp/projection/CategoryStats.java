package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.common.Category;

/**
 * Interface projection for aggregating nutrition and price snapshots within a particular day and category.
 */
public interface CategoryStats extends Stats {
    Category getCategory();
}