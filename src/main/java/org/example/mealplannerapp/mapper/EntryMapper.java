package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.entry.request.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.request.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {FoodMapper.class})
public interface EntryMapper {

    @SubclassMapping(source = FoodEntryCreateRequest.class, target = FoodEntry.class)
    Entry createFromRequest(EntryCreateRequest request);

    // UPDATE METHOD PENDING

    @SubclassMapping(source = FoodEntry.class, target = FoodEntryResponse.class)
    EntryResponse generateResponse(Entry entry);

    // TO DO: Reorder Mapping?
    // TO DO: ignore annotations
    // TO DO: generateResponse might need different handling?
    
}