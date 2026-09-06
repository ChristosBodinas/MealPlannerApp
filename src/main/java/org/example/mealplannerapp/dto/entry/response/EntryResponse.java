package org.example.mealplannerapp.dto.entry.response;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

import java.math.BigDecimal;

/**
 * Base response DTO interface for displaying {@link Entry} data.
 */
public sealed interface EntryResponse permits
        FoodEntryResponse,
        ExerciseEntryResponse {
    Long id();
    String name();
    Category category();
    int position();
    BigDecimal calories();
    BigDecimal protein();
    BigDecimal carbs();
    BigDecimal fat();
    BigDecimal fiber();
    BigDecimal price();
}
