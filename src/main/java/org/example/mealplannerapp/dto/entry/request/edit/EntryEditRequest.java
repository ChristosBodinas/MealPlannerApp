package org.example.mealplannerapp.dto.entry.request.edit;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FoodEntryEditRequest.class, name = "FOOD")
})
public sealed interface EntryEditRequest permits FoodEntryEditRequest {
}
