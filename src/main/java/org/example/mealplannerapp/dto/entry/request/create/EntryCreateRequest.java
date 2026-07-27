package org.example.mealplannerapp.dto.entry.request.create;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Request DTO interface for creating new entries.
 * EntryCreateRequest
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodEntryCreateRequest.class, name = "FOOD")
})
public sealed interface EntryCreateRequest permits FoodEntryCreateRequest {
}
