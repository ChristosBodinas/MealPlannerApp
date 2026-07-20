package org.example.mealplannerapp.dto.day;

import java.util.List;

import jakarta.validation.Valid;

public record DayGoalsListRequest(
    List<@Valid DayGoalsRequest> goals
) {
}
