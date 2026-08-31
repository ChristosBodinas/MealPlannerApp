package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.day.response.DayResponse;
import org.example.mealplannerapp.entity.Day;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DayMapper {

    DayResponse toResponse(Day day);
}
