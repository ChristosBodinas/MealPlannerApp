package org.example.mealplannerapp.dto.entry.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

import java.math.BigDecimal;

/**
 * Base response DTO interface for displaying {@link Entry} data.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodEntryResponse.class, name = "FOOD"),
        @JsonSubTypes.Type(value = ExerciseEntryResponse.class, name = "EXERCISE")
})
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
