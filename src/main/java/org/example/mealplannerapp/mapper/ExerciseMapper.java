package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.LevelResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.embeddable.ExerciseLevel;
import org.example.mealplannerapp.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    // HELPER METHODS
    ExerciseLevel toLevel(LevelRequest request);
    LevelResponse toResponse(ExerciseLevel level);

    // MAIN METHODS
    @Mapping(target = "levels", source = "levels", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    Exercise toExercise(ExerciseRequest request);

    void update(@MappingTarget Exercise exercise, ExerciseRequest request); // TODO: Does this need @Mapping?

    ExerciseResponse toResponse(Exercise exercise);
    ListedExerciseResponse toListedResponse(Exercise exercise);
}
