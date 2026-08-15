package org.example.mealplannerapp.dto.user;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;

/**
 * Request DTO for editing a user's account details.
 */
@Builder
public record UserDetailsRequest(
    // TODO: Validation.
    String nickname,
    Gender gender,
    Sex sex,
    LocalDate birthDate,
    BigDecimal height
) {

}
