package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.projection.stats.Stats;
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

    private final PlanMapper planMapper;

    // createPlan

    // editPlan

    // redistributeGoals

    // deletePlan
    public void deletePlan(User user, Long planId) {
        if (!planRepository.existsByIdVerified(user.getId(), planId)) {
            throw new ResourceNotFoundException("Requested plan (id: " + planId + ") not found.");
        }

        entryRepository.deleteAllByPlan(planId);
        dayRepository.deleteAllByPlan(planId);
        planRepository.deleteById(planId);
    }

    // retrievePlanInfo

    // retrievePlanSummary
    public void retrievePlanSummary(User user, Long planId) {
        if (!planRepository.existsByIdVerified(user.getId(), planId)) {
            throw new ResourceNotFoundException("Requested plan (id: " + planId + ") not found.");
        }

        Stats planStats = entryRepository.extractStatsByPlan(planId);
        // TODO: dayRepository.sumGoalsByPlan
    }

    // generateShoppingList
}
