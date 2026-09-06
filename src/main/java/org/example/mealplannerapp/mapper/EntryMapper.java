package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.entry.request.create.CreateEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateExerciseEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateFoodEntryRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EditExerciseEntryRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EditFoodEntryRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.ExerciseEntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {FoodMapper.class, ExerciseMapper.class},
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface EntryMapper {

    @SubclassMapping(source = CreateFoodEntryRequest.class, target = FoodEntry.class)
    @SubclassMapping(source = CreateExerciseEntryRequest.class, target = ExerciseEntry.class)
    Entry toEntry(CreateEntryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget FoodEntry entry, EditFoodEntryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget ExerciseEntry entry, EditExerciseEntryRequest request);

    @SubclassMapping(source = FoodEntry.class, target = FoodEntryResponse.class)
    @SubclassMapping(source = ExerciseEntry.class, target = ExerciseEntryResponse.class)
    EntryResponse toResponse(Entry entry);

}