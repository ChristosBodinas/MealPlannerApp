package org.example.mealplannerapp.dto.entry.request.edit;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Base request DTO interface for submitting {@link Entry} update data.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EditFoodEntryRequest.class, name = "FOOD"),
        @JsonSubTypes.Type(value = EditExerciseEntryRequest.class, name = "EXERCISE")
})
public sealed interface EditEntryRequest permits
        EditFoodEntryRequest,
        EditExerciseEntryRequest {
}
