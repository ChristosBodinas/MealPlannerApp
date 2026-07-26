package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.constants.ActivityLevel;
import org.example.mealplannerapp.entity.Plan;

import java.util.ArrayList;

public class PlanTestFixtures {

    private static final String DEFAULT_NAME = "August Weight Loss";
    private static final double DEFAULT_START_WEIGHT = 98.5;
    private static final double DEFAULT_DESIRED_CHANGE = -1.00;
    private static final ActivityLevel DEFAULT_ACTIVITY_LEVEL = ActivityLevel.SEDENTARY;
    private static final double DEFAULT_PROTEIN_RATIO = 0.25;
    private static final double DEFAULT_CARBS_RATIO = 0.40;

    /**
     * Method for building {@link Plan} entities.
     *
     * @return a Plan builder with all fields except {@code id} and {@code user} filled in
     * ({@code days} contains an empty list)
     */
    public static Plan.PlanBuilder defaultPlanBuilder() {
        return Plan.builder()
                .name(DEFAULT_NAME)
                .days(new ArrayList<>())
                .startWeight(DEFAULT_START_WEIGHT)
                .desiredChange(DEFAULT_DESIRED_CHANGE)
                .activityLevel(DEFAULT_ACTIVITY_LEVEL)
                .proteinRatio(DEFAULT_PROTEIN_RATIO)
                .carbsRatio(DEFAULT_CARBS_RATIO);
    }

}
