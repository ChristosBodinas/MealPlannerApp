package org.example.mealplannerapp.mapper;

import java.util.List;
import java.util.Locale.Category;

import org.example.mealplannerapp.dto.day.response.CategoryStatsResponse;
import org.example.mealplannerapp.dto.day.response.DayGoalsResponse;
import org.example.mealplannerapp.dto.day.response.DayStatsResponse;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.Goals;
import org.example.mealplannerapp.projection.Stats;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DayMapper {

    DayStatsResponse toDayStatsResponse(Stats dayStats);

    DayGoalsResponse toDayGoalsResponse(Goals dayGoals);

    CategoryStatsResponse toCategoryStatsResponse(CategoryStats categoryStats);
    
    DaySummaryResponse toSummaryResponse(List<CategoryStats> categoryStats, Stats dayStats, Goals dayGoals);

}