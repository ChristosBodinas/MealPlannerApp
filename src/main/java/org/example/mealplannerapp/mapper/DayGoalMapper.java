package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.day.request.DayGoalRequest;
import org.example.mealplannerapp.entity.Day;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DayGoalMapper {

    // Main methods.
    Day createFromRequest(DayGoalRequest request);
}