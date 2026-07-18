package org.example.mealplannerapp.fixtures;

import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.entity.Plan;

public class PlanTestFixtures {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_NAME = "My Diet";
    private static final double DEFAULT_START_WEIGHT = 95.0;
    private static final double DEFAULT_DESIRED_CHANGE = -0.50;
    private static final int DEFAULT_NUMBER_OF_DAYS = 7;
    private static final double DEFAULT_PROTEIN_RATIO = 0.20;
    private static final double DEFAULT_CARBS_RATIO = 0.40; 

    /**
     * Method for building {@link Plan} entities.
     * @return a Plan builder with default values for all fields except {@code user} and {@code days}
     */
    public static Plan.PlanBuilder defaultPlanBuilder() {
            return Plan.builder()
                .id(DEFAULT_ID)
                .startWeight(DEFAULT_START_WEIGHT)
                .desiredChange(DEFAULT_DESIRED_CHANGE)
                .proteinRatio(DEFAULT_PROTEIN_RATIO)
                .carbsRatio(DEFAULT_CARBS_RATIO);
    }

    /**
     * Method for building {@link PlanCreateRequest} DTOs.
     * @return a PlanCreateRequest builder with default values for all fields
     */
    public static PlanCreateRequest.PlanCreateRequestBuilder defaultPlanCreateRequestBuilder() {
        return PlanCreateRequest.builder()
            .name(DEFAULT_NAME)
            .startWeight(DEFAULT_START_WEIGHT)
            .desiredChange(DEFAULT_DESIRED_CHANGE)
            .numberOfDays(DEFAULT_NUMBER_OF_DAYS)
            .proteinRatio(DEFAULT_PROTEIN_RATIO)
            .carbsRatio(DEFAULT_CARBS_RATIO);
    }

    /**
     * Method for building {@link PlanInfoResponse} DTOs.
     * @return a PlanInfoResponse builder with default values for all fields
     */
    public static PlanInfoResponse.PlanInfoResponseBuilder defaultPlanInfoResponseBuilder() {
        return PlanInfoResponse.builder()
            .id(DEFAULT_ID)
            .startWeight(DEFAULT_START_WEIGHT)
            .desiredChange(DEFAULT_DESIRED_CHANGE)
            .proteinRatio(DEFAULT_PROTEIN_RATIO)
            .carbsRatio(DEFAULT_CARBS_RATIO);
    }
    
}
