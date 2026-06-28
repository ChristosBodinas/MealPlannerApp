package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.mapper.DayGoalMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.dto.day.request.DayGoalRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DayService {

    private final DayRepository dayRepository;
    private final DayGoalMapper dayGoalMapper;
    //private final EntryService entryService;

    // createDaysForPlan
    public void createDaysForPlan(Long planId, int numberOfDays, List<DayGoalRequest> requests) {
        dayRepository.saveAll(
            requests.stream().map(dayGoalMapper::createFromRequest).toList());
    }

    // updateDailyGoals

    // deleteDaysByPlanId

    // retrieveDayById

    // duplicateDaysByPlanId

    // generateCategorySummary

    // generateDaySummary

}