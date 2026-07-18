package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.entity.User;
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

    /* THOUGHTS:

     */

    // create plan and days
    public PlanInfoResponse createPlan(User user, PlanCreateRequest request) {
        return new PlanInfoResponse();
    }

    // duplicate plan

    // edit plan parameters

    // redistributeDayGoals

    // delete plan

    // retrievePlanGoals

    // generateShoppingList

}
