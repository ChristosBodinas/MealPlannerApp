package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.DaySummaryResponse;
import org.example.mealplannerapp.dto.GoalsResponse;
import org.example.mealplannerapp.dto.StatsResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.projection.stats.Stats;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuxiliaryMapper {

    StatsResponse toStatsResponse(Stats stats);

    GoalsResponse toGoalsResponse(Day day);

}
