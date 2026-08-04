package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.entity.Plan;

import java.util.HashSet;

public class PlanTestFixtures {

    // TODO: Expand builder method once the Plan entity is complete.

    /**
     * Method for building {@link Plan} entities.
     *
     * @return a Plan builder with an empty Day set and no {@code id} or {@code user}
     */
    public static Plan.PlanBuilder defaultPlanBuilder() {
        return Plan.builder()
                .days(new HashSet<>());
    }

}