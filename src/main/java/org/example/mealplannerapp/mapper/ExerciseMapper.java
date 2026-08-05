package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.IntensityRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.IntensityResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.embeddable.ExerciseIntensity;
import org.example.mealplannerapp.entity.Exercise;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    // EXERCISE INTENSITY MAPPING
    ExerciseIntensity toIntensity(IntensityRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<ExerciseIntensity> toIntensities(Set<IntensityRequest> requests);

    IntensityResponse toIntensityResponse(ExerciseIntensity intensity);

    // EXERCISE MAPPING
    @Mapping(target = "user", ignore = true)
    Exercise toExercise(ExerciseRequest request);

    @Mapping(target = "user", ignore = true)
    void update(@MappingTarget Exercise exercise, ExerciseRequest request);

    ExerciseResponse toResponse(Exercise exercise);

    ListedExerciseResponse toListedResponse(Exercise exercise);
}