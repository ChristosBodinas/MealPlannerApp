package org.example.mealplannerapp.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.mapper.PlanMapper;
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
    @Mock private PlanMapper planMapper;
    
    @InjectMocks private PlanService planService;

    // UNIVERSAL VARIABLES
    private User user;

    @Nested
    class createPlan {

        private PlanCreateRequest request;

        @BeforeEach
        void prepareTests() {
            user = defaultUserBuilder().build();
        }

        @Test
        @DisplayName("Given a valid input, creates a new Plan and the associated Days, and evenly distributes the nutrient goals.")
        void happyFlow() {
            // Arrange
            request = defaultPlanCreateRequestBuilder().build();
            Plan created = defaultPlanBuilder().user(user).build();
            Plan saved = new Plan();
            PlanInfoResponse expected = defaultPlanInfoResponseBuilder().build();

            when(planMapper.createFromRequest(request)).thenReturn(created);
            when(planRepository.save(created)).thenReturn(saved);
            when(planMapper.generateResponse(saved)).thenReturn(expected);


            // Act
            PlanInfoResponse response = planService.createPlan(user, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            assertThat(created.getDays()).isNotNull();
            assertThat(created.getDays().size()).isEqualTo(request.numberOfDays());
            // TO DO: examine that the calculations work correctly? Or leave that for a different test.
        }

        @Test
        @DisplayName("Given invalid nutrient ratios, throws some kind of exception.")
        void nutrientRatioMismatch() {
            // Arrange
            request = defaultPlanCreateRequestBuilder().proteinRatio(0.8).carbsRatio(0.7).build();

            // Act + Assert
            assertThatThrownBy(() -> planService.createPlan(user, request))
                .isInstanceOf(RuntimeException.class);
            verifyNoInteractions(planRepository);
        }

    }

    @Nested
    class editPlan {

        @BeforeEach
        void prepareTests() {

        }

        @Test
        @DisplayName("Given a valid entryId and request, updates the requested Plan's parameters and recalculates daily goals.")
        void happyFlow() {

        }

        @Test
        @DisplayName("Given an invalid entryId, throws a ResourceNotFoundException. ")
        void planNotFound() {

        }
        
    }

    @Nested
    class deletePlan {

    }

}
