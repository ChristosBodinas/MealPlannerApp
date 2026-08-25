package org.example.mealplannerapp.dto.user;

import lombok.Builder;
import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for displaying a user's account details.
 */
@Builder
public record UserDetailsResponse(
        String username,
        String nickname,
        Gender gender,
        Sex sex,
        LocalDate birthDate,
        BigDecimal height
) {
}