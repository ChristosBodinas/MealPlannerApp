package org.example.mealplannerapp.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.mealplannerapp.fixtures.PlanTestFixtures.*;
import static org.example.mealplannerapp.fixtures.UserTestFixtures.*;

@ExtendWith(MockitoExtension.class)
public class PlanServiceUnitTests {

    // MOCKS AND INJECTION
    @Mock private PlanRepository planRepository;
    @Mock private DayRepository dayRepository;
    @Mock private EntryRepository entryRepository;
    @Mock private PlanMapper planMapper;
    
    @InjectMocks private PlanService planService;

    // UNIVERSAL VARIABLES
    private User user;

    private static final Long USER_ID = 1L;
    private static final Long PLAN_ID = 55L;

    @Nested
    class createPlan {

        private PlanCreateRequest request;

        private static final int NUMBER_OF_DAYS = 5;

        @BeforeEach
        void prepareTests() {
            user = defaultUserBuilder().build();
        }

        @Test
        @DisplayName("Given a valid input, creates a new Plan and the associated Days, and evenly distributes the nutrient goals.")
        void happyFlow() {
            // Arrange
            request = defaultPlanCreateRequestBuilder().numberOfDays(NUMBER_OF_DAYS).build();
            Plan created = spy(defaultPlanBuilder().user(user).build());
            Plan saved = new Plan();
            PlanInfoResponse expected = defaultPlanInfoResponseBuilder().build();

            when(planMapper.createFromRequest(request)).thenReturn(created);
            when(planRepository.save(created)).thenReturn(saved);
            when(planMapper.generateResponse(saved)).thenReturn(expected);

            // Act
            PlanInfoResponse response = planService.createPlan(user, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(created).initializeDays(NUMBER_OF_DAYS); // This is a service unit test. We don't care if the entity method works here.
        }

        @Test
        @DisplayName("Given invalid nutrient ratios, throws some kind of exception.")
        void nutrientRatioMismatch() {
            // Arrange
            request = defaultPlanCreateRequestBuilder().proteinRatio(0.8).carbsRatio(0.7).build();

            // Act + Assert
            assertThatThrownBy(() -> planService.createPlan(user, request))
                .isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(planRepository);
        }

    }

    @Nested
    class editPlan {

        private PlanEditRequest request;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = defaultPlanEditRequestBuilder().build();

            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid entryId and request, updates the requested Plan's parameters and recalculates daily goals.")
        void happyFlow() {
            // Arrange
            Plan found = spy(defaultPlanBuilder().build());
            PlanInfoResponse expected = defaultPlanInfoResponseBuilder().build();

            when(planRepository.findByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.of(found));
            when(planMapper.generateResponse(found)).thenReturn(expected);

            // Act
            PlanInfoResponse response = planService.editPlan(user, PLAN_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(planMapper).updateFromRequest(found, request);
            verify(found).distributeDailyGoals();
        }

        @Test
        @DisplayName("Given an invalid entryId, throws a ResourceNotFoundException. ")
        void planNotFound() {
            // Arrange
            when(planRepository.findByIdVerified(USER_ID, PLAN_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> planService.editPlan(user, PLAN_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(planMapper);
        }
        
    }

    @Nested
    class deletePlan {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(PLAN_ID);
        }

        @Test
        @DisplayName("Given a valid planId, deletes the Plan along with all the associated Days and Entries.")
        void happyFlow() {
            // Arrange
            when(planRepository.existsByIdVerified(USER_ID, PLAN_ID)).thenReturn(true);

            // Act
            planService.deletePlan(user, PLAN_ID);

            // Assert
            verify(planRepository).existsByIdVerified(USER_ID, PLAN_ID);
            verify(entryRepository).deleteAllInPlan(PLAN_ID);
            verify(dayRepository).deleteAllInPlan(PLAN_ID);
            verify(planRepository).deleteById(PLAN_ID);
        }

        @Test
        @DisplayName("Given an invalid planId, throws a ResourceNotFoundException.")
        void planNotFound() {
            // Arrange
            when(planRepository.existsByIdVerified(USER_ID, PLAN_ID)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> planService.deletePlan(user, PLAN_ID))
                .isInstanceOf(ResourceNotFoundException.class);
            verify(planRepository).existsByIdVerified(USER_ID, PLAN_ID);
            verifyNoMoreInteractions(planRepository);
            verifyNoInteractions(entryRepository)
            verifyNoInteractions(dayRepository);
        }

    }

    @Nested
    class retrievePlanInfo {

    }

    @Nested
    class retrievePlanTotals {

    }

    @Nested
    class retrievePlanGoals {

    }

}
