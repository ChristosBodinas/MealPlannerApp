package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.entry.request.EntryReorderRequest;
import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.RequestEntityMismatchException;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {FoodMapper.class})
public interface EntryMapper {

    @SubclassMapping(source = FoodEntryCreateRequest.class, target = FoodEntry.class)
    Entry createFromRequest(EntryCreateRequest request);

    void updateFoodEntry(@MappingTarget FoodEntry entry, FoodEntryEditRequest request);

    default void updateFromRequest(@MappingTarget Entry entry, EntryEditRequest request) {
        if (entry instanceof FoodEntry && request instanceof FoodEntryEditRequest) {
            updateFoodEntry((FoodEntry) entry, (FoodEntryEditRequest) request);
        } else {
            throw new RequestEntityMismatchException("Submitted data does not fit the requested entity.");
        }
    }

    @Mapping(source = "entryId", ignore = true)
    void repositionEntry(@MappingTarget Entry entry, EntryReorderRequest request);

    @SubclassMapping(source = FoodEntry.class, target = FoodEntryResponse.class)
    EntryResponse generateResponse(Entry entry);

    // TO DO: Reorder Mapping?
    // TO DO: ignore annotations
    // TO DO: generateResponse might need different handling?
    
}