package org.example.mealplannerapp.dto.entry.response;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.entry.Entry;

import java.math.BigDecimal;

/**
 * Response DTO interface for displaying {@link Entry} data along
 * with the referenced {@link Food} or {@link Exercise}.
 */
public sealed interface EntryResponse permits
        FoodEntryResponse,
        ExerciseEntryResponse {
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
