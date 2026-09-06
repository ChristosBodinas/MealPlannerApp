package org.example.mealplannerapp.dto.entry.request.edit;

import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Base request DTO interface for submitting {@link Entry} update data.
 */
public sealed interface EditEntryRequest permits
        EditFoodEntryRequest,
        EditExerciseEntryRequest {
}
