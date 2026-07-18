package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;

    private final PlanMapper planMapper;

    // create plan and days
    public PlanInfoResponse createPlan(User user, PlanCreateRequest request) {
        // Verification
        if (request.proteinRatio() + request.carbsRatio() >= 1.00) {
            // TO DO: replace with a custom exception
            throw new RuntimeException("Invalid argument values for protein/carb/fat distribution");
        }
        
        // Create plan from request and set the plan's user.
        Plan plan = planMapper.createFromRequest(request);
        plan.setUser(user);

        // Create days and distribute goals.
        plan.initializeDays(request.numberOfDays());

        Plan saved = planRepository.save(plan);
        return planMapper.generateResponse(saved);
    }

    @Transactional
    public PlanInfoResponse editPlan(User user, Long planId, PlanEditRequest request) {
        Plan plan = planRepository.findByIdVerified(user.getId(), planId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") was not found."));

        planMapper.updateFromRequest(plan, request);
        plan.distributeDailyGoals();

        return planMapper.generateResponse(plan);  
    }

    // redistributeDayGoals

    // delete plan

    // retrievePlanGoals

    // generateShoppingList

}
