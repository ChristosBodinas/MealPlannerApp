package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.plan.request.CreatePlanRequest;
import org.example.mealplannerapp.dto.plan.request.EditPlanRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.IncompleteProfileException;
import org.example.mealplannerapp.exception.InvalidTotalException;
import org.example.mealplannerapp.exception.PlanNotFeasibleException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.repository.PlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    private final PlanMapper planMapper;

    private void throwIfIncompleteProfile(User user) {
        if (user.getSex() == null || user.getBirthDate() == null || user.getHeight() == null) {
            throw new IncompleteProfileException(
                    "Account must have sex, birth date, and height filled out before meal plans can be created.");
        }
    }

    private void throwIfInvalidRatios(BigDecimal proteinRatio, BigDecimal carbsRatio, BigDecimal fatRatio) {
        BigDecimal ratioTotal = proteinRatio
                .add(carbsRatio)
                .add(fatRatio);

        if (ratioTotal.compareTo(BigDecimal.ONE) != 0) {
            throw new InvalidTotalException("Protein, carbs, and fat ratios should add up to 1.");
        }
    }

    private void throwIfPlanNotFeasible(Plan plan, int numberOfDays) {
        BigDecimal TDEE = plan.computeTDEE();
        BigDecimal averageDailyDeficit = plan.computeAverageDailyDeficit(numberOfDays);

        if (TDEE.compareTo(averageDailyDeficit) <= 0) {
            throw new PlanNotFeasibleException("The suggested plan is not nutritionally feasible.");
        }
    }

    private void computeDailyGoals(User user, Plan plan, Day day, int numberOfDays) {
        day.setTargetCalories(plan.getTargetCalories()
                .divide(BigDecimal.valueOf(numberOfDays), RoundingMode.HALF_UP));
        day.setTargetProtein(plan.getTargetProtein()
                .divide(BigDecimal.valueOf(numberOfDays), RoundingMode.HALF_UP));
        day.setTargetCarbs(plan.getTargetCarbs()
                .divide(BigDecimal.valueOf(numberOfDays), RoundingMode.HALF_UP));
        day.setTargetFat(plan.getTargetFat()
                .divide(BigDecimal.valueOf(numberOfDays), RoundingMode.HALF_UP));
        day.setTargetFiber(BigDecimal.valueOf(user.getSex().getDailyFiberIntake()));
    }

    public PlanResponse createPlan(
            User user, CreatePlanRequest request
    ) {
        throwIfIncompleteProfile(user);
        throwIfInvalidRatios(request.proteinRatio(), request.carbsRatio(), request.fatRatio());

        Plan plan = planMapper.toPlan(request);
        throwIfPlanNotFeasible(plan, request.numberOfDays());

        plan.setUser(user);
        plan.computeNutritionTargets(request.numberOfDays());

        plan.setDays(new LinkedHashSet<>(request.numberOfDays()));
        for (int i = 1; i <= request.numberOfDays(); i++) {
            Day day = Day.builder().plan(plan).position(i).build();
            computeDailyGoals(user, plan, day, request.numberOfDays());
            plan.getDays().add(day);
        }

        planRepository.save(plan);
        return planMapper.toResponse(plan);
    }

    // TODO: THIS IS WIP.
    @Transactional
    public PlanResponse editPlanParameters(
            User user, Long planId, EditPlanRequest request
    ) {
        // TODO: Insert check to ensure all ratios are present and assign them if so.
        throwIfInvalidRatios(request.proteinRatio(), request.carbsRatio(), request.fatRatio());

        Long userId = user.getId();
        Plan plan = planRepository.fetchByIdVerified(userId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        planMapper.update(plan, request);

        int numberOfDays = plan.getDays().size();

        throwIfPlanNotFeasible(plan, numberOfDays);
        plan.computeNutritionTargets(numberOfDays);

        for (Day day : plan.getDays()) {
            computeDailyGoals(user, plan, day, numberOfDays);
        }

        return planMapper.toResponse(plan);
    }

    // retrievePlan
    public PlanResponse retrievePlan(
            User user, Long planId
    ) {
        Long userId = user.getId();
        Plan plan = planRepository.fetchByIdVerified(userId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        return planMapper.toResponse(plan);
    }

    // searchPlans
    public Page<ListedPlanResponse> searchPlans(
            User user, String searchText, Pageable pageable
    ) {
        Long userId = user.getId();
        return planRepository.fetchShallowByUserAndText(userId, searchText, pageable)
                .map(planMapper::toListedResponse);
    }
}
