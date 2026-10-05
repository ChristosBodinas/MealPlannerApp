package org.example.mealplannerapp.dto.entry.response.listed;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.entry.Entry;

import java.math.BigDecimal;

/**
 * Response DTO interface for displaying {@link Entry} data without
 * the referenced {@link Food} or {@link Exercise} data.
 */
public sealed interface ListedEntryResponse permits
        ListedFoodEntryResponse,
        ListedExerciseEntryResponse {
    Long id();
    Category category();
    int position();
    String name();
    BigDecimal calories();
    BigDecimal protein();
    BigDecimal carbs();
    BigDecimal fat();
    BigDecimal fiber();
    BigDecimal price();
}
