package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.plan.request.CreatePlanRequest;
import org.example.mealplannerapp.dto.plan.request.EditPlanRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanSummaryResponse;
import org.example.mealplannerapp.dto.plan.response.ShopItemResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.exception.*;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.projection.DayStats;
import org.example.mealplannerapp.projection.Stats;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.PlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;

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
        BigDecimal tdee = plan.computeTDEE();
        BigDecimal averageDailyDeficit = plan.computeAverageDailyDeficit(numberOfDays);

        if (tdee.compareTo(averageDailyDeficit) <= 0) {
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

    /**
     * Creates a new {@link Plan} owned by {@code user} as well as the {@link Day} entities that comprise
     * said plan using the submitted {@code request} data, calculates nutritional targets for the whole plan
     * and for each individual days, then saves them all to the database.
     * <p>Daily nutritional targets are calculated by evenly distributing the plan-wide targets.</p>
     *
     * @param user    the user making the request
     * @param request the submitted plan creation data
     * @return the full data of the new plan and the days that comprise it
     * @throws IncompleteProfileException if the user hasn't filled in the account details needed for
     *                                    nutritional target calculations
     * @throws InvalidTotalException      if the submitted macronutrient ratios do not properly add up to 100%
     * @throws PlanNotFeasibleException   if the submitted data results in zero or negative nutrition targets
     */
    public PlanResponse createPlan(
            User user, CreatePlanRequest request
    ) {
        throwIfIncompleteProfile(user);
        throwIfInvalidRatios(request.proteinRatio(), request.carbsRatio(), request.fatRatio());

        Plan plan = planMapper.toPlan(request);
        plan.setUser(user);

        throwIfPlanNotFeasible(plan, request.numberOfDays());
        plan.computeNutritionTargets(request.numberOfDays());

        plan.setDays(new LinkedHashSet<>(request.numberOfDays()));
        for (int i = 1; i <= request.numberOfDays(); i++) {
            Day day = Day.builder().plan(plan).position(i).build();
            computeDailyGoals(user, plan, day, request.numberOfDays());
            plan.getDays().add(day);
        }

        Plan saved = planRepository.save(plan);
        return planMapper.toResponse(saved);
    }

    /**
     * Updates the {@link Plan} identified by {@code planId} and owned by {@code user} using the
     * submitted {@code request} data, then recalculates plan-wide and daily nutritional targets.
     * <p>Daily nutritional targets are calculated by evenly distributing the plan-wide targets.</p>
     *
     * @param user    the user making the request
     * @param planId  the identifier of the plan to be updated
     * @param request the submitted plan update data
     * @return the full data of the new plan and the days that comprise it
     * @throws InvalidTotalException      if the submitted macronutrient ratios do not properly add up to 100%
     * @throws IncompleteRequestException if some but not all macronutrient ratios are submitted
     * @throws ResourceNotFoundException  if {@code planId} does not correspond to an existing
     *                                    plan owned by {@code user}
     * @throws PlanNotFeasibleException   if the submitted data results in zero or negative nutrition targets
     */
    @Transactional
    public PlanResponse editPlanParameters(
            User user, Long planId, EditPlanRequest request
    ) {

        boolean allRatiosAreNull = request.proteinRatio() == null && request.carbsRatio() == null && request.fatRatio() == null;
        boolean allRatiosAreNonNull = request.proteinRatio() != null && request.carbsRatio() != null && request.fatRatio() != null;

        BiConsumer<Plan, EditPlanRequest> mapperMethod;

        if (allRatiosAreNull) {
            mapperMethod = planMapper::updateExcludeRatios;
        } else if (allRatiosAreNonNull) {
            throwIfInvalidRatios(request.proteinRatio(), request.carbsRatio(), request.fatRatio());
            mapperMethod = planMapper::updateIncludeRatios;
        } else {
            throw new IncompleteRequestException("Ratios were only partially filled out.");
        }

        Long userId = user.getId();
        Plan plan = planRepository.fetchByIdVerified(userId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        mapperMethod.accept(plan, request);

        int numberOfDays = plan.getDays().size();

        throwIfPlanNotFeasible(plan, numberOfDays);
        plan.computeNutritionTargets(numberOfDays);

        for (Day day : plan.getDays()) {
            computeDailyGoals(user, plan, day, numberOfDays);
        }

        return planMapper.toResponse(plan);
    }

    /**
     * Deletes the {@link Plan} identified by {@code planId} and owned by {@code user},
     * along with all the {@link Day} and {@link Entry} entities that comprise it.
     *
     * @param user   the user making the request
     * @param planId the identifier of the plan to be deleted
     * @throws ResourceNotFoundException if {@code planId} does not correspond to an existing
     *                                   plan owned by {@code user}
     */
    @Transactional
    public void deletePlan(
            User user, Long planId
    ) {
        Long userId = user.getId();

        if (!planRepository.existsByIdVerified(userId, planId)) {
            throw new ResourceNotFoundException(
                    "Requested plan (id: " + planId + ") not found.");
        }

        entryRepository.deleteByPlan(planId);
        dayRepository.deleteByPlan(planId);
        planRepository.deleteById(planId);
    }

    /**
     * Retrieves the {@link Plan} identified by {@code planId} and owned by {@code user}, along
     * with the {@link Day} entities that comprise it.
     *
     * @param user   the user making the request
     * @param planId the identifier of the plan to be retrieved
     * @return the full data of the requested plan and the days that comprise it
     * @throws ResourceNotFoundException if {@code planId} does not correspond to an existing
     *                                   plan owned by {@code user}
     */
    @Transactional(readOnly = true)
    public PlanResponse retrievePlan(
            User user, Long planId
    ) {
        Long userId = user.getId();
        Plan plan = planRepository.fetchByIdVerified(userId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested plan (id: " + planId + ") not found."));

        return planMapper.toResponse(plan);
    }

    /**
     * Retrieves a page of {@link Plan} entities owned by {@code user} whose names contain {@code searchText}.
     * If {@code searchText} is empty or {@code null}, plans are retrieved regardless of their names.
     * Text-matching is case-insensitive.
     * <p>Results are paginated and sorted according to {@code pageable}.</p>
     *
     * @param user       the user making the request
     * @param searchText the text to match against plan names
     * @param pageable   the pagination and sorting parameters to apply to the results
     * @return a page containing the top-level data of the retrieved plans,
     * excluding the days that comprise each one
     */
    @Transactional(readOnly = true)
    public Page<ListedPlanResponse> searchPlans(
            User user, String searchText, Pageable pageable
    ) {
        Long userId = user.getId();
        return planRepository.fetchShallowByUserAndText(userId, searchText, pageable)
                .map(planMapper::toListedResponse);
    }

    /**
     * Produces a summary of nutrition goals, per-day nutrition/price stats, and total
     * nutrition/price stats for the {@link Plan} identified by {@code planId} and
     * owned by {@code user}.
     *
     * @param user   the user making the request
     * @param planId the identifier of the plan to be summarized
     * @return a summary of the requested plan's goals, stats, and per-day stats
     * @throws ResourceNotFoundException if {@code planId} does not correspond to an
     *                                   existing plan owned by {@code user}
     */
    @Transactional(readOnly = true)
    public PlanSummaryResponse summarizePlan(
            User user, Long planId
    ) {
        Long userId = user.getId();

        Plan plan = planRepository.fetchByIdVerified(userId, planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested plan (id: " + planId + ") not found."));

        List<DayStats> dayStats = entryRepository.summarizeDaysByPlan(planId);

        Stats planStats = new Stats(
                dayStats.stream().map(DayStats::calories).reduce(BigDecimal.ZERO, BigDecimal::add),
                dayStats.stream().map(DayStats::protein).reduce(BigDecimal.ZERO, BigDecimal::add),
                dayStats.stream().map(DayStats::carbs).reduce(BigDecimal.ZERO, BigDecimal::add),
                dayStats.stream().map(DayStats::fat).reduce(BigDecimal.ZERO, BigDecimal::add),
                dayStats.stream().map(DayStats::fiber).reduce(BigDecimal.ZERO, BigDecimal::add),
                dayStats.stream().map(DayStats::price).reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        // Fill out day stats with "empty" days.
        List<DayStats> completeDayStats;
        if (dayStats.size() < plan.getDays().size()) {
            completeDayStats = new ArrayList<>(plan.getDays().size());
            Map<Long, DayStats> mappedDayStats = dayStats.stream().collect(Collectors.toMap(
                    DayStats::id, Function.identity()));

            for (Day day : plan.getDays()) {
                if (mappedDayStats.containsKey(day.getId())) {
                    completeDayStats.add(mappedDayStats.get(day.getId()));
                } else {
                    completeDayStats.add(new DayStats(day.getId(),
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
                }
            }
        } else {
            completeDayStats = dayStats;
        }

        return planMapper.toSummaryResponse(plan, planStats, completeDayStats);
    }

    /**
     * Generates a shopping list by aggregating all the food quantities contained in the
     * {@link Plan} identified by {@code planId} and owned by {@code user}.
     *
     * @param user   the user making the request
     * @param planId the identifier of the plan to generate a shopping list for
     * @return a list of food brands + names along with their total quantity throughout the plan
     * @throws ResourceNotFoundException if {@code planId} does not correspond to an
     *                                   existing plan owned by {@code user}
     */
    @Transactional(readOnly = true)
    public List<ShopItemResponse> generateShoppingList(
            User user, Long planId
    ) {
        Long userId = user.getId();

        if (!planRepository.existsByIdVerified(userId, planId)) {
            throw new ResourceNotFoundException("Requested plan (id: " + planId + ") not found.");
        }

        return entryRepository.extractShoppingListByPlan(planId).stream()
                .map(planMapper::toShopItemResponse)
                .toList();
    }
}