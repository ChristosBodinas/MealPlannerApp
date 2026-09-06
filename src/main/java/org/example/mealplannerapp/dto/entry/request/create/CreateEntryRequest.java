package org.example.mealplannerapp.dto.entry.request.create;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Base request DTO interface for submitting {@link Entry} creation data.
 */
public sealed interface CreateEntryRequest permits
        CreateFoodEntryRequest,
        CreateExerciseEntryRequest {
    Category category();
}
