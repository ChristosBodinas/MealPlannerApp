package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.entity.Exercise;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Exercise toExercise(ExerciseRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    void update(@MappingTarget Exercise exercise, ExerciseRequest request);

    ExerciseResponse toResponse(Exercise exercise);

    ListedExerciseResponse toListedResponse(Exercise exercise);
}
