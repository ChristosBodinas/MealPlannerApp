package org.example.mealplannerapp.dto.entry.request.create;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.mealplannerapp.common.Category;

/**
 * Request DTO interface for creating new entries.
 * EntryCreateRequest
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodEntryCreateRequest.class, name = "FOOD")
})
public sealed interface EntryCreateRequest permits FoodEntryCreateRequest {

    Category category();
}