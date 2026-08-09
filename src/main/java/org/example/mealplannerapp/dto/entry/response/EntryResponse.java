package org.example.mealplannerapp.dto.entry.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Response DTO interface for displaying entries, along with their referenced data.
 * EntryResponse
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodEntryResponse.class, name = "FOOD"),
        @JsonSubTypes.Type(value = ExerciseEntryResponse.class, name = "EXERCISE")
})
public sealed interface EntryResponse permits FoodEntryResponse, ExerciseEntryResponse {
}