package org.example.mealplannerapp.service;

import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.mapper.PlanMapper;
import org.example.mealplannerapp.mapper.PlanMapperImpl;
import org.example.mealplannerapp.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

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

    // TESTS PROPER
    @BeforeEach
    void prepareServiceAndUser() {
        planMapper = new PlanMapperImpl();
        planService = new PlanService(planRepository, planMapper);

        myUser = defaultUser().id(USER_ID).build();
    }

    @Nested
    @DisplayName("createPlan")
    class CreatePlan {}

    @Nested
    @DisplayName("editPlanParameters")
    class EditPlanParameters {}

    @Nested
    @DisplayName("retrievePlan")
    class RetrievePlan {}

    @Nested
    @DisplayName("searchPlans")
    class SearchPlans {}
}
