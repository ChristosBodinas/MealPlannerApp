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

    // createPlan(User user, PlanCreateRequest request)
    // output -> Plan + Days (days are just goals)

    // editPlanParameters

    // redistributeGoals

    // deletePlan

    // retrievePlan

    // searchPlans

    // summarizePlan

    // generateShoppingList
    
}
