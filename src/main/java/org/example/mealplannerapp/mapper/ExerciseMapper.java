package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.LevelResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    // HELPER METHODS
    EffortLevel toLevel(LevelRequest request);

    LevelResponse toResponse(EffortLevel level);

    // MAIN METHODS
    @Mapping(target = "levels", source = "levels", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    Exercise toExercise(ExerciseRequest request);

    @Mapping(target = "levels", source = "levels", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    void update(@MappingTarget Exercise exercise, ExerciseRequest request);

    ExerciseResponse toResponse(Exercise exercise);

    ListedExerciseResponse toListedResponse(Exercise exercise);
}
