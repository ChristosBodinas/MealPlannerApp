package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.common.Gender;
import org.example.mealplannerapp.common.Sex;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An entity that represents an application user's personal information
 * and relevant biological data.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Identifier used for authentication.
     */
    @Column(name = "auth_id", nullable = false, unique = true)
    @Setter(AccessLevel.NONE)
    private String authId;

    /**
     * Username used for authentication.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Name by which the app addresses the user.
     */
    @Column(length = 50)
    private String nickname;

    /**
     * Gender identity by which the app addresses the user.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    /**
     * User's biological sex. Used in nutrition-related calculations.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Sex sex;

    /**
     * User's date of birth. Used in nutrition-related calculations.
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * User's height in centimeters. Used in nutrition-related calculations.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    public int computeAgeInYears() {
        return birthDate.until(LocalDate.now()).getYears();
    }

}