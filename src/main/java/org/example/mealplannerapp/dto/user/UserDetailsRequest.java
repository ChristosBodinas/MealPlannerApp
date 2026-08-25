package org.example.mealplannerapp.dto.user;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for editing a user's account details.
 */
@Builder
public record UserDetailsRequest(

        @NotBlank(message = "User's nickname must contain at least 1 character.")
        @Size(max = 50, message = "User's nickname cannot exceed 50 characters.")
        String nickname,

        @NotNull(message = "User's gender identity cannot be null.")
        Gender gender,

        @NotNull(message = "User's biological sex cannot be null.")
        Sex sex,

        @NotNull(message = "User's date of birth cannot be null.")
        @Past(message = "User's date of birth must be a past date.")
        LocalDate birthDate,

        @NotNull(message = "User's height cannot be null.")
        @Positive(message = "User's height must be a positive number.")
        @Digits(integer = 3, fraction = 2, message = "User's height must have at most 3 integer digits and 2 decimal digits.")
        BigDecimal height
) {
}