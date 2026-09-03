package org.example.mealplannerapp.service;

import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.dto.plan.request.CreatePlanRequest;
import org.example.mealplannerapp.dto.plan.request.EditPlanRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.*;
import org.example.mealplannerapp.mapper.DayMapper;
import org.example.mealplannerapp.mapper.DayMapperImpl;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.mapper.PlanMapperImpl;
import org.example.mealplannerapp.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDay;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.*;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link PlanService} methods using a mocked
 * {@link PlanRepository} and a real (non-mocked) {@link PlanMapper}
 * interface.
 */
@ExtendWith(MockitoExtension.class)
public class PlanServiceUnitTests {

    // CONSTANTS
    private static final long USER_ID = 1L;
    private static final long PLAN_ID = 99L;

    // BEANS
    private PlanService planService;
    private PlanMapper planMapper;

    @Mock
    private PlanRepository planRepository;

    // VARIABLES
    private User myUser;

    // HELPER METHODS
    private Plan prepareDefaultPlan() {
        Plan plan = defaultPlan().id(PLAN_ID).user(myUser).build();
        plan.computeNutritionTargets(3);

        for (int i = 1; i <= 3; i++) {
            Day day = defaultDay().plan(plan).position(i).build();
            plan.getDays().add(day);
        }

        return plan;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareServiceAndUser() {
        DayMapper dayMapper = new DayMapperImpl();
        planMapper = new PlanMapperImpl(dayMapper);
        planService = new PlanService(planRepository, planMapper);

        myUser = defaultUser().id(USER_ID).build();
    }

    @Nested
    @DisplayName("createPlan")
    class CreatePlan {

        @Captor
        ArgumentCaptor<Plan> captor;

        @Test
        @DisplayName("Given a valid request, creates a new plan owned by the current user, along with its associated days.")
        void planCreated() {
            // Arrange
            CreatePlanRequest request = defaultCreatePlanRequest().build();
            Plan saved = prepareDefaultPlan();

            when(planRepository.save(any(Plan.class))).thenReturn(saved);

            // Act
            PlanResponse response = planService.createPlan(myUser, request);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(planMapper.toResponse(saved));

            verify(planRepository, description("Plan should be saved to the database."))
                    .save(captor.capture());

            Plan created = captor.getValue();

            assertThat(created.getUser()).as("New plan should belong to the current user")
                    .isEqualTo(myUser);

            assertThat(created).as("New plan fields should match request data.")
                    .usingRecursiveComparison()
                    .comparingOnlyFields("name", "startWeight", "desiredWeightLoss", "activityLevel",
                            "proteinRatio", "carbsRatio", "fatRatio")
                    .isEqualTo(request);  // This only ensures that the mapping was correct.

            assertThat(created.getDays().size()).as("New plan should have the right number of days.")
                    .isEqualTo(request.numberOfDays());

            assertThat(created.getDays())
                    .as("Plan days should have unique positions.")
                    .extracting(Day::getPosition)
                    .containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, request.numberOfDays()).boxed().toList());
        }

        @Test
        @DisplayName("Given a valid request, correctly calculates the nutrition targets for each individual day.")
        void dailyGoalsComputedCorrectly() {
            // Arrange
            CreatePlanRequest request = defaultCreatePlanRequest().build();
            Plan saved = prepareDefaultPlan();

            when(planRepository.save(any(Plan.class))).thenReturn(saved);

            // Act
            planService.createPlan(myUser, request);

            // Assert
            verify(planRepository).save(captor.capture());

            Plan created = captor.getValue();
            BigDecimal numDays = BigDecimal.valueOf(created.getDays().size());

            BigDecimal dayCalories = created.getTargetCalories().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayProtein = created.getTargetProtein().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayCarbs = created.getTargetCarbs().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayFat = created.getTargetFat().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayFiber = BigDecimal.valueOf(myUser.getSex().getDailyFiberIntake());

            assertThat(created.getDays())
                    .as("Daily nutrition targets should be correctly calculated.")
                    .allSatisfy(day -> {
                        assertThat(day.getTargetCalories()).isEqualByComparingTo(dayCalories);
                        assertThat(day.getTargetProtein()).isEqualByComparingTo(dayProtein);
                        assertThat(day.getTargetCarbs()).isEqualByComparingTo(dayCarbs);
                        assertThat(day.getTargetFat()).isEqualByComparingTo(dayFat);
                        assertThat(day.getTargetFiber()).isEqualByComparingTo(dayFiber);
                    });
        }

        @Test
        @DisplayName("If the user's account details are missing the required fields, throws an IncompleteProfileException.")
        void incompleteProfile() {
            // Arrange
            CreatePlanRequest request = defaultCreatePlanRequest().build();
            myUser.setHeight(null);

            // Act + Assert
            assertThatThrownBy(() -> planService.createPlan(myUser, request))
                    .as("Method should throw an IncompleteProfileException.")
                    .isInstanceOf(IncompleteProfileException.class);

            verify(planRepository, never().description("Nothing should be saved to the database."))
                    .save(any(Plan.class));
        }

        @Test
        @DisplayName("If the submitted nutrient ratios don't add up to 1, throws an InvalidTotalException.")
        void invalidRatios() {
            // Arrange
            CreatePlanRequest request = defaultCreatePlanRequest()
                    .proteinRatio(BigDecimal.valueOf(0.5))
                    .carbsRatio(BigDecimal.valueOf(0.5))
                    .fatRatio(BigDecimal.valueOf(0.5))
                    .build();

            // Act + Assert
            assertThatThrownBy(() -> planService.createPlan(myUser, request))
                    .as("Method should throw an InvalidTotalException.")
                    .isInstanceOf(InvalidTotalException.class);

            verify(planRepository, never().description("Nothing should be saved to the database."))
                    .save(any(Plan.class));
        }

        @Test
        @DisplayName("If the desired weight loss is not possible within the allotted number of days, throws a PlanNotFeasibleException.")
        void planNotFeasible() {
            // Arrange
            CreatePlanRequest request = defaultCreatePlanRequest()
                    .desiredWeightLoss(BigDecimal.valueOf(9.9)) // It's physically impossible to lose this match in 1-14 days.
                    .build();

            // Act + Assert
            assertThatThrownBy(() -> planService.createPlan(myUser, request))
                    .as("Method should throw a PlanNotFeasibleException.")
                    .isInstanceOf(PlanNotFeasibleException.class);

            verify(planRepository, never().description("Nothing should be saved to the database."))
                    .save(any(Plan.class));
        }

    }

    @Nested
    @DisplayName("editPlanParameters")
    class EditPlanParameters {

        private ActivityLevel anyOtherActivityLevel(ActivityLevel initialLevel) {
            return EnumSet.allOf(ActivityLevel.class)
                    .stream()
                    .filter(level -> level != initialLevel)
                    .findFirst()
                    .orElse(null);
        }

        private EditPlanRequest prepareNullRatiosRequest(Plan plan) {
            return EditPlanRequest.builder()
                    .name(plan.getName() + "_edited")
                    .startWeight(plan.getStartWeight().add(BigDecimal.valueOf(1.0)))
                    .desiredWeightLoss(plan.getDesiredWeightLoss().add(BigDecimal.valueOf(0.1)))
                    .activityLevel(anyOtherActivityLevel(plan.getActivityLevel()))
                    .proteinRatio(null)
                    .carbsRatio(null)
                    .fatRatio(null)
                    .build();
        }

        private EditPlanRequest prepareValidRatiosRequest(Plan plan) {
            return EditPlanRequest.builder()
                    .name(plan.getName() + "_edited")
                    .startWeight(plan.getStartWeight().add(BigDecimal.valueOf(1.0)))
                    .desiredWeightLoss(plan.getDesiredWeightLoss().add(BigDecimal.valueOf(0.1)))
                    .activityLevel(anyOtherActivityLevel(plan.getActivityLevel()))
                    .proteinRatio(plan.getProteinRatio().add(BigDecimal.valueOf(0.05)))
                    .carbsRatio(plan.getCarbsRatio().add(BigDecimal.valueOf(0.05)))
                    .fatRatio(plan.getFatRatio().add(BigDecimal.valueOf(-0.10)))
                    .build();
        }

        @Test
        @DisplayName("Given an existing planId owned by the current user and a request with all ratios set to null, " +
                "leaves ratios unchanged and updates the other fields, then recalculates nutrition targets.")
        void ratiosAllNull() {
            // Arrange
            Plan fetched = prepareDefaultPlan();
            EditPlanRequest request = prepareNullRatiosRequest(fetched);

            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.of(fetched));

            // Act
            PlanResponse response = planService.editPlanParameters(myUser, PLAN_ID, request);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(planMapper.toResponse(fetched));

            assertThat(fetched).as("Updated plan fields should match request fields.")
                    .usingRecursiveComparison()
                    .comparingOnlyFields("name", "startWeight", "desiredWeightLoss", "activityLevel")
                    .isEqualTo(request);
        }

        @Test
        @DisplayName("Given an existing planId owned by the current user and a request with all ratios set to non-null values, " +
                "updates all fields, then recalculates nutrition targets.")
        void ratiosAllNonNull() {
            // Arrange
            Plan fetched = prepareDefaultPlan();
            EditPlanRequest request = prepareValidRatiosRequest(fetched);

            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.of(fetched));

            // Act
            PlanResponse response = planService.editPlanParameters(myUser, PLAN_ID, request);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(planMapper.toResponse(fetched));

            assertThat(fetched).as("Updated plan fields should match request fields.")
                    .usingRecursiveComparison()
                    .comparingOnlyFields("name", "startWeight", "desiredWeightLoss", "activityLevel",
                            "proteinRatio", "carbsRatio", "fatRatio")
                    .isEqualTo(request);
        }

        @Test
        @DisplayName("Does not update other fields whose equivalent request value is null.")
        void ignoresOtherNullFields() {
            // Arrange
            Plan fetched = prepareDefaultPlan();
            EditPlanRequest request = defaultEditPlanRequest()
                    .name(fetched.getName() + "_edited")
                    .activityLevel(null)
                    .build();

            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.of(fetched));

            // Act
            planService.editPlanParameters(myUser, PLAN_ID, request);

            // Assert
            Plan original = prepareDefaultPlan();

            assertThat(fetched.getName()).as("Plan name should be changed.")
                    .isNotEqualTo(original.getName());

            assertThat(fetched.getActivityLevel()).as("Activity level should be unchanged.")
                    .isEqualTo(original.getActivityLevel());
        }

        @Test
        @DisplayName("Given a valid request, correctly calculates the nutrition targets for each day.")
        void dailyGoalsComputedCorrectly() {
            // Arrange
            Plan fetched = prepareDefaultPlan();
            EditPlanRequest request = prepareValidRatiosRequest(fetched);

            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.of(fetched));

            // Act
            planService.editPlanParameters(myUser, PLAN_ID, request);

            // Assert
            BigDecimal numDays = BigDecimal.valueOf(fetched.getDays().size());

            BigDecimal dayCalories = fetched.getTargetCalories().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayProtein = fetched.getTargetProtein().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayCarbs = fetched.getTargetCarbs().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayFat = fetched.getTargetFat().divide(numDays, RoundingMode.HALF_UP);
            BigDecimal dayFiber = BigDecimal.valueOf(myUser.getSex().getDailyFiberIntake());

            assertThat(fetched.getDays())
                    .as("Daily nutrition targets should be correctly calculated.")
                    .allSatisfy(day -> {
                        assertThat(day.getTargetCalories()).isEqualByComparingTo(dayCalories);
                        assertThat(day.getTargetProtein()).isEqualByComparingTo(dayProtein);
                        assertThat(day.getTargetCarbs()).isEqualByComparingTo(dayCarbs);
                        assertThat(day.getTargetFat()).isEqualByComparingTo(dayFat);
                        assertThat(day.getTargetFiber()).isEqualByComparingTo(dayFiber);
                    });
        }

        @Test
        @DisplayName("Given a request with all ratios set to non-null values that don't add up to 1, " +
                "throws an InvalidTotalException.")
        void invalidRatios() {
            EditPlanRequest request = defaultEditPlanRequest()
                    .proteinRatio(BigDecimal.valueOf(0.5))
                    .carbsRatio(BigDecimal.valueOf(0.5))
                    .fatRatio(BigDecimal.valueOf(0.5))
                    .build();

            // Act + Assert
            assertThatThrownBy(() -> planService.editPlanParameters(myUser, PLAN_ID, request))
                    .as("Method should throw an InvalidTotalException.")
                    .isInstanceOf(InvalidTotalException.class);
        }

        @Test
        @DisplayName("Given a request with some but not all ratios set to null, throws an IncompleteDataException.")
        void incompleteRatios() {
            // Arrange
            EditPlanRequest request = defaultEditPlanRequest()
                    .proteinRatio(BigDecimal.valueOf(0.5))
                    .carbsRatio(null)
                    .build();

            // Act + Assert
            assertThatThrownBy(() -> planService.editPlanParameters(myUser, PLAN_ID, request))
                    .as("Method should throw an IncompleteDataException.")
                    .isInstanceOf(IncompleteRequestException.class);
        }

        @Test
        @DisplayName("Given a valid request but a non-existent or non-owned planId, throws a ResourceNotFoundException.")
        void planNotFound() {
            // Arrange
            EditPlanRequest request = defaultEditPlanRequest().build();
            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> planService.editPlanParameters(myUser, PLAN_ID, request))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("If the desired weight loss is not possible within the allotted number of days, throws a PlanNotFeasibleException.")
        void planNotFeasible() {
            // Arrange
            EditPlanRequest request = defaultEditPlanRequest()
                    .desiredWeightLoss(BigDecimal.valueOf(9.9)) // It's physically impossible to lose this match in 1-14 days.
                    .build();
            Plan fetched = prepareDefaultPlan();

            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.of(fetched));

            // Act + Assert
            assertThatThrownBy(() -> planService.editPlanParameters(myUser, PLAN_ID, request))
                    .as("Method should throw a PlanNotFeasibleException.")
                    .isInstanceOf(PlanNotFeasibleException.class);
        }

    }

    @Nested
    @DisplayName("retrievePlan")
    class RetrievePlan {

        @Test
        @DisplayName("Given an existing planId owned by the current user, returns a response.")
        void planRetrieved() {
            // Arrange
            Plan fetched = prepareDefaultPlan();

            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.of(fetched));

            // Act
            PlanResponse response = planService.retrievePlan(myUser, PLAN_ID);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(planMapper.toResponse(fetched));
        }

        @Test
        @DisplayName("Given a non-existent or non-owned planId, throws a ResourceNotFoundException.")
        void planNotFound() {
            // Arrange
            when(planRepository.fetchByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> planService.retrievePlan(myUser, PLAN_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("searchPlans")
    class SearchPlans {

        @Test
        @DisplayName("Returns a page of listed responses.")
        void matchesReturned() {
            // Arrange
            List<Plan> plans = new ArrayList<>(List.of(
                    defaultPlan().name("match1").user(myUser).build(),
                    defaultPlan().name("match2").user(myUser).build(),
                    defaultPlan().name("match3").user(myUser).build()));

            Pageable pageable = PageRequest.of(0, 3);
            Page<Plan> plansPage = new PageImpl<>(plans, pageable, 3);

            when(planRepository.fetchShallowByUserAndText(USER_ID, "match", pageable))
                    .thenReturn(plansPage);

            // Act
            Page<ListedPlanResponse> response = planService.searchPlans(myUser, "match", pageable);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(plansPage.map(planMapper::toListedResponse));
        }

    }

}
