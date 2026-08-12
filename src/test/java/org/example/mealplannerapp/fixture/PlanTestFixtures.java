package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.entity.Plan;

import java.util.HashSet;

public class PlanTestFixtures {

    // TODO: Expand builder method once the Plan entity is complete.
    private static final String DEFAULT_NAME = "Weight Loss Plan";
    private static final double DEFAULT_START_WEIGHT = 100.0;
    private static final double DEFAULT_TARGET_LOSS = 0.5;
    private static final ActivityLevel DEFAULT_ACTIVITY_LEVEL = ActivityLevel.LIGHT;

    /**
     * Method for building {@link Plan} entities.
     *
     * @return a Plan builder with an empty Day set and no {@code id} or {@code user}
     */
    public static Plan.PlanBuilder defaultPlanBuilder() {
        return Plan.builder()
                .days(new HashSet<>())
                .name(DEFAULT_NAME)
                .startWeight(DEFAULT_START_WEIGHT)
                .targetLoss(DEFAULT_TARGET_LOSS)
                .activityLevel(DEFAULT_ACTIVITY_LEVEL);
    }

}