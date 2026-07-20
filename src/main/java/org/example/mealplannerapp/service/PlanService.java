package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.example.mealplannerapp.dto.day.DayGoalsListRequest;
import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.dto.plan.PlanNutrientsResponse;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.projection.PlanNutrients;
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
            throw new IllegalArgumentException("Invalid argument values for protein/carb/fat distribution");
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
                .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        planMapper.updateFromRequest(plan, request);
        plan.distributeDailyGoals();

        return planMapper.generateResponse(plan);  
    }

    @Transactional
    public void redistributeDayGoals(User user, Long planId, DayGoalsListRequest request) {
        Plan plan = planRepository.findByIdVerified(user.getId(), planId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));
        
        // Verify that the request (1) has the right length and (2) adds up correctly.

        // Convert

        // call the Plan method to do the thing
    }

    @Transactional
    public void deletePlan(User user, Long planId) {
        if (!planRepository.existsByIdVerified(user.getId(), planId)) {
            throw new ResourceNotFoundException("Requested plan (id: " + planId + ") not found.");
        }
        entryRepository.deleteAllInPlan(planId);
        dayRepository.deleteAllInPlan(planId);
        planRepository.deleteById(planId);
    }

    public PlanInfoResponse retrievePlanInfo(User user, Long planId) {
        Plan plan = planRepository.findByIdVerified(user.getId(), planId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        return planMapper.generateResponse(plan);

    }

    public PlanNutrientsResponse retrievePlanTotals(User user, Long planId) {
        if (!planRepository.existsByIdVerified(user.getId(), planId)) {
            throw new ResourceNotFoundException("Requested plan (id: " + planId + ") not found.");
        }

        PlanNutrients nutrients = entryRepository.summarizeByPlan(planId);
        return planMapper.generateNutrientsResponse(nutrients);
    }

    public PlanNutrientsResponse retrievePlanGoals(User user, Long planId) {
        if (!planRepository.existsByIdVerified(user.getId(), planId)) {
            throw new ResourceNotFoundException("Requested plan (id: " + planId + ") not found.");
        }

        PlanNutrients nutrients = dayRepository.summarizeGoalsInPlan(planId);
        return planMapper.generateNutrientsResponse(nutrients);
    }

    // generateShoppingList

}
