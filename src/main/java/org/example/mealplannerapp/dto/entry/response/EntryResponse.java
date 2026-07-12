package org.example.mealplannerapp.dto.entry.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodEntryResponse.class, name = "FOOD")
})
public sealed interface EntryResponse permits FoodEntryResponse {
}