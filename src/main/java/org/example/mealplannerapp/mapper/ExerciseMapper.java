package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.request.LevelRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.LevelResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.embeddable.ExerciseLevel;
import org.example.mealplannerapp.entity.Exercise;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    // EXERCISE LEVEL MAPPING
    ExerciseLevel toLevel(LevelRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<ExerciseLevel> toLevels(Set<LevelRequest> requests);

    LevelResponse toLevelResponse(ExerciseLevel level);

    // EXERCISE MAPPING
    @Mapping(target = "user", ignore = true)
    Exercise toExercise(ExerciseRequest request);

    @Mapping(target = "user", ignore = true)
    void update(@MappingTarget Exercise exercise, ExerciseRequest request);

    ExerciseResponse toResponse(Exercise exercise);

    ListedExerciseResponse toListedResponse(Exercise exercise);

}