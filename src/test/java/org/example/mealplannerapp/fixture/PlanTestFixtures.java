package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.entity.Plan;

import java.math.BigDecimal;

public class PlanTestFixtures {

    private static final String DEFAULT_NAME = "My Diet";
    private static final BigDecimal DEFAULT_START_WEIGHT = BigDecimal.valueOf(90.0);
    private static final BigDecimal DEFAULT_DESIRED_WEIGHT_LOSS = BigDecimal.valueOf(0.5);
    private static final ActivityLevel DEFAUL_ACTIVITY_LEVEL = ActivityLevel.LIGHT;
    private static final BigDecimal DEFAULT_PROTEIN_RATIO = BigDecimal.valueOf(0.2);
    private static final BigDecimal DEFAULT_CARBS_RATIO = BigDecimal.valueOf(0.4);
    private static final BigDecimal DEFAULT_FAT_RATIO = BigDecimal.valueOf(0.4);

    /**
     * Builds a {@link Plan} entity fixture for testing.
     *
     * @return a Plan builder with null {@code id} and {@code user}, an empty {@code days} set,
     * and default values in all parameter fields. Nutrition target fields must be calculated
     * separately.
     */
    public static Plan.PlanBuilder defaultPlan() {
        return Plan.builder()
                .name(DEFAULT_NAME)
                //.days(new LinkedHashSet<>())
                .startWeight(DEFAULT_START_WEIGHT)
                .desiredWeightLoss(DEFAULT_DESIRED_WEIGHT_LOSS)
                .activityLevel(DEFAUL_ACTIVITY_LEVEL)
                .proteinRatio(DEFAULT_PROTEIN_RATIO)
                .carbsRatio(DEFAULT_CARBS_RATIO)
                .fatRatio(DEFAULT_FAT_RATIO);
    }

}