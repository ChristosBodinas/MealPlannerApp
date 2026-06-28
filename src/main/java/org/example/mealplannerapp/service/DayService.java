package org.example.mealplannerapp.service;

import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.dto.request.DayGoalRequest;

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