package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.ExerciseEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.request.edit.ExerciseEntryEditRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.ExerciseEntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.MappingMismatchException;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {FoodMapper.class, ExerciseMapper.class},
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface EntryMapper {

    // CREATE FROM REQUEST
    @SubclassMapping(source = FoodEntryCreateRequest.class, target = FoodEntry.class)
    @SubclassMapping(source = ExerciseEntryCreateRequest.class, target = ExerciseEntry.class)
    Entry toEntry(EntryCreateRequest request);

    // EDIT FROM REQUEST
    default void update(@MappingTarget Entry entry, EntryEditRequest request) {
        if (entry instanceof FoodEntry && request instanceof FoodEntryEditRequest) {
            updateFoodEntry((FoodEntry) entry, (FoodEntryEditRequest) request);
        } else if (entry instanceof ExerciseEntry && request instanceof ExerciseEntryEditRequest) {
            updateExerciseEntry((ExerciseEntry) entry, (ExerciseEntryEditRequest) request);
        } else {
            throw new MappingMismatchException("Submitted data type does not match the target entity.");
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFoodEntry(@MappingTarget FoodEntry entry, FoodEntryEditRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExerciseEntry(@MappingTarget ExerciseEntry entry, ExerciseEntryEditRequest request);

    // GENERATE RESPONSE
    @SubclassMapping(source = FoodEntry.class, target = FoodEntryResponse.class)
    @SubclassMapping(source = ExerciseEntry.class, target = ExerciseEntryResponse.class)
    EntryResponse toResponse(Entry entry);

}