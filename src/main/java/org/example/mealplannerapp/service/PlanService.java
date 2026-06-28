package org.example.mealplannerapp.service;

import org.example.mealplannerapp.repository.PlanRepository;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final DayService dayService;

    // createNewPlan -> calls dayService.createDaysForPlan

    // updatePlanParametersById -> optionally(?) calls dayService.updateDailyGoals

    // deletePlanById -> calls dayService.deleteDaysByPlanId

    // retrievePlanById -> might call dayService.retrieveDayById on the first day?

    // retrieveAllPlans

    // retrievePlansByTextSearch

    // duplicatePlanById -> calls duplicateDaysByPlanId

    // generatePlanGoals

    // generatePlanSummary

    // generateShoppingList

}