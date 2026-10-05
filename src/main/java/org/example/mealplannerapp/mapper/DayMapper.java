package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.day.response.DayResponse;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.Stats;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DayMapper {

    DayResponse toResponse(Day day);

    DaySummaryResponse toSummaryResponse(Day day, Stats dayStats, List<CategoryStats> categoryStats);
}
