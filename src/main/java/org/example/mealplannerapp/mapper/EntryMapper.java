package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.ExerciseEntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.dto.entry.response.listed.ListedEntryResponse;
import org.example.mealplannerapp.dto.entry.response.listed.ListedExerciseEntryResponse;
import org.example.mealplannerapp.dto.entry.response.listed.ListedFoodEntryResponse;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {FoodMapper.class, ExerciseMapper.class},
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface EntryMapper {

    @SubclassMapping(source = FoodEntry.class, target = FoodEntryResponse.class)
    @SubclassMapping(source = ExerciseEntry.class, target = ExerciseEntryResponse.class)
    EntryResponse toResponse(Entry entry);

    @SubclassMapping(source = FoodEntry.class, target = ListedFoodEntryResponse.class)
    @SubclassMapping(source = ExerciseEntry.class, target = ListedExerciseEntryResponse.class)
    ListedEntryResponse toListedResponse(Entry entry);

}
