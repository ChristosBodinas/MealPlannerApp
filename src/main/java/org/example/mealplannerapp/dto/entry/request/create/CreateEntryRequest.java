package org.example.mealplannerapp.dto.entry.request.create;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Base request DTO interface for submitting {@link Entry} creation data.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateFoodEntryRequest.class, name = "FOOD"),
        @JsonSubTypes.Type(value = CreateExerciseEntryRequest.class, name = "EXERCISE")
})
public sealed interface CreateEntryRequest permits
        CreateFoodEntryRequest,
        CreateExerciseEntryRequest {
    Category category();
}
