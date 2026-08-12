package org.example.mealplannerapp.dto.day.request;

import java.util.List;

import jakarta.validation.Valid;

public record GoalsListRequest(
    List<@Valid DayGoalsRequest> dayGoalsRequests
) {
}
