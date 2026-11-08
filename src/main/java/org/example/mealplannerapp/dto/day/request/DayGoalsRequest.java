package org.example.mealplannerapp.dto.day.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Request DTO for submitting daily nutrition goals data.
 */
@Builder
public record DayGoalsRequest(

        @NotNull(message = "Target calories cannot be null.")
        @PositiveOrZero(message = "Target calories cannot be a negative number.")
        @Digits(integer = 4, fraction = 2, message = "Target calories must have at most 4 integer digits and 2 decimal places.")
        BigDecimal targetCalories,

        @NotNull(message = "Target protein cannot be null.")
        @PositiveOrZero(message = "Target protein cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Target protein must have at most 3 integer digits and 2 decimal places.")
        BigDecimal targetProtein,

        @NotNull(message = "Target carbs cannot be null.")
        @PositiveOrZero(message = "Target carbs cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Target carbs must have at most 3 integer digits and 2 decimal places.")
        BigDecimal targetCarbs,

        @NotNull(message = "Target fat cannot be null.")
        @PositiveOrZero(message = "Target fat cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Target fat must have at most 3 integer digits and 2 decimal places.")
        BigDecimal targetFat,

        @NotNull(message = "Target fiber cannot be null.")
        @PositiveOrZero(message = "Target fiber cannot be a negative number.")
        @Digits(integer = 2, fraction = 2, message = "Target fiber must have at most 3 integer digits and 2 decimal places.")
        BigDecimal targetFiber
) {

}
