package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.entry.request.FoodEntryCreateRequest;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {FoodMapper.class})
public interface FoodMapper {

    @SubclassMapping(source = FoodEntryCreateRequest.class, target = FoodEntry.class)
    Entry createFromRequest(EntryCreateRequest request);

    @SubclassMapping(source = FoodEntryEditRequest.class, target = FoodEntry.class)
    void updateFromRequest(@MappingTarget Entry entry, EntryEditRequest request);

    @SubclassMapping(source = FoodEntry.class, target = FoodEntryResponse.class)
    EntryResponse generateResponse(Entry entry);

    // TO DO: Reorder Mapping?
    // TO DO: ignore annotations
    // TO DO: generateResponse might need different handling?
    
}