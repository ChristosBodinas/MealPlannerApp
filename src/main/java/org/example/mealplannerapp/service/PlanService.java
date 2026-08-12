package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.example.mealplannerapp.dto.day.request.DayGoalsRequest;
import org.example.mealplannerapp.dto.day.request.GoalsListRequest;
import org.example.mealplannerapp.dto.plan.request.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.request.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanSummaryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.projection.Goals;
import org.example.mealplannerapp.projection.Stats;
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

    private void verifyMacroRatiosFit(double proteinRatio, double carbsRatio) {
        if (proteinRatio + carbsRatio >= 1.0) {
            throw new ServiceValidationException("Macronutrient ratios must add up to 100%.");
        }
    }

    private void verifyDailyGoalsAddUp(Plan plan, List<DayGoalsRequest> requests) {
        boolean mismatch = false;
        double epsilon = 0.01;
        
        if (requests.stream().mapToDouble(DayGoalsRequest::targetCalories).sum() - plan.getTargetCalories() > epsilon) {
            mismatch = true;
        } else if (requests.stream().mapToDouble(DayGoalsRequest::targetProtein).sum() - plan.getTargetProtein() > epsilon) {
            mismatch = true;
        } else if (requests.stream().mapToDouble(DayGoalsRequest::targetCarbs).sum() - plan.getTargetCarbs() > epsilon) {
            mismatch = true;
        } else if (requests.stream().mapToDouble(DayGoalsRequest::targetFat).sum() - plan.getTargetFat() > epsilon) {
            mismatch = true;
        } else if (requests.stream().mapToDouble(DayGoalsRequest::targetFiber).sum() - plan.getTargetFiber() > epsilon) {
            mismatch = true;
        }

        if (mismatch = true) {
            throw new ServiceValidationException("Daily goals do not correctly add up to total plan goals.");
        }
    }

    // TODO: Might put these into the Day class itself.
    private void computeDailyGoals(Day day, Plan plan, int numberOfDays) {
        day.setTargetCalories(plan.getTargetCalories() / numberOfDays);
        day.setTargetProtein(plan.getTargetProtein() / numberOfDays);
        day.setTargetCarbs(plan.getTargetCarbs() / numberOfDays);
        day.setTargetFat(plan.getTargetFat() / numberOfDays);
        day.setTargetFiber(plan.getTargetFiber() / numberOfDays);
    }

    private void computeDailyGoals(Day day, DayGoalsRequest request) {
        day.setTargetCalories(request.targetCalories());
        day.setTargetProtein(request.targetProtein());
        day.setTargetCarbs(request.targetCarbs());
        day.setTargetFat(request.targetFat());
        day.setTargetFiber(request.targetFiber());
    }

    // createPlan(User user, PlanCreateRequest request)
    public PlanResponse createPlan(User user, PlanCreateRequest request) {
        verifyMacroRatiosFit(request.proteinRatio(), request.carbsRatio());

        Plan plan = planMapper.toPlan(request);
        plan.setUser(user);
        plan.computeNutritionGoals(request.proteinRatio(), request.carbsRatio());

        // CREATE DAYS SET
        plan.setDays(new LinkedHashSet<>(request.numberOfDays()));

        // CREATE EACH DAY AND ADD IT
        for (int i = 1; i <= request.numberOfDays(); i++) {
            Day day = new Day();
            day.setPosition(i);
            day.setPlan(plan);
            computeDailyGoals(day, plan, request.numberOfDays());
            plan.getDays().add(day);
        }

        Plan saved = planRepository.save(plan);
        return planMapper.toResponse(saved);
    }

    @Transactional
    public PlanResponse editPlan(User user, Long planId, PlanEditRequest request) {
        verifyMacroRatiosFit(request.proteinRatio(), request.carbsRatio());
        
        Plan plan = planRepository.fetchByIdVerified(user.getId(), planId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));
        
        planMapper.update(plan, request);
        plan.computeNutritionGoals(request.proteinRatio(), request.carbsRatio());

        for (Day day : plan.getDays()) {
            computeDailyGoals(day, plan, plan.getDays().size());
        }

        return planMapper.toResponse(plan);
    }

    @Transactional
    public void redistributeGoals(User user, Long planId, GoalsListRequest request) {
        Plan plan = planRepository.fetchByIdVerified(user.getId(), planId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        verifyDailyGoalsAddUp(plan, request.dayGoalsRequests());

        int index = 1;
        for (Day day : plan.getDays()) {
            computeDailyGoals(day, request.dayGoalsRequests().get(index));
            index++;
        }
    }

    @Transactional
    public void deletePlan(User user, Long planId) {
        if (!planRepository.existsByIdVerified(user.getId(), planId)) {
            throw new ResourceNotFoundException("Requested plan (id: " + planId + ") not found.");
        }

        entryRepository.deleteByPlan(planId);
        dayRepository.deleteByPlan(planId);
        planRepository.deleteById(planId);
    }

    public PlanResponse retrievePlan(User user, Long planId) {
        Plan plan = planRepository.fetchByIdVerified(user.getId(), planId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        return planMapper.toResponse(plan);
    }

    public List<ListedPlanResponse> searchPlans(User user, String search) {
        return planRepository.fetchShallowByUserAndText(user.getId(), search)
            .stream()
            .map(planMapper::toListedResponse)
            .toList();
    }

    // summarizePlan
    public PlanSummaryResponse summarizePlan(User user, Long planId) {
        Plan plan = planRepository.fetchByIdVerified(user.getId(), planId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        Goals planGoals = plan.retrieveGoals();

        Stats planStats = entryRepository.sumSnapshotsByPlan(planId);

        return planMapper.toSummaryResponse(planStats, planGoals);
    }

    // generateShoppingList
    public void generateShoppingList(User user, Long planId) {
        // TODO: Write this method.
    }
    
}
