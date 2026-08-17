package org.example.mealplannerapp.dto.food.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record PriceRequest(

    @NotBlank(message = "Vendor name/description cannot be blank.")
    @Size(max = 10, message = "Vendor name/description cannot exceed 10 characters.")
    String vendorName,

    @NotNull(message = "Purchase price cannot be null.")
    @PositiveOrZero(message = "Purchase price cannot be a negative number.")    // TODO: We let this be zero to facilitate logging foods the user has free access to.
    @Digits(integer = 3, fraction = 2, message = "Purchase price cannot have more than 3 integer digits and 2 decimal places.")
    BigDecimal purchasePrice,

    @NotNull(message = "Purchase grams cannot be null.")
    @Positive(message = "Purchase grams must be a positive number.")
    @Digits(integer = 3, fraction = 2, message = "Purchase grams cannot have more than 3 integer digits and 2 decimal places.")
    BigDecimal purchaseGrams
) {
}
