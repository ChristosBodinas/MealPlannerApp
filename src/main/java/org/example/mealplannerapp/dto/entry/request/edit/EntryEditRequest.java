package org.example.mealplannerapp.dto.entry.request.edit;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Request DTO interface for editing entries.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodEntryEditRequest.class, name = "FOOD"),
        @JsonSubTypes.Type(value = ExerciseEntryEditRequest.class, name = "EXERCISE")
})
public sealed interface EntryEditRequest permits FoodEntryEditRequest, ExerciseEntryEditRequest {
}