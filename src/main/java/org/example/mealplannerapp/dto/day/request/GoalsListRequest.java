package org.example.mealplannerapp.dto.day.request;

import jakarta.validation.Valid;

import java.util.List;

public record GoalsListRequest(
        List<@Valid DayGoalsRequest> dayGoalsRequests
) {
}
