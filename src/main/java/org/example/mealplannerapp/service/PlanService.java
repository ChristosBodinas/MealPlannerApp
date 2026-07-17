package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.PlanRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;

    // create plan and days

    // duplicate plan

    // edit plan parameters

    // redistributeDayGoals

    // delete plan

    // retrievePlanGoals

    // generateShoppingList

}
