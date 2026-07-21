package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.constants.Gender;
import org.example.mealplannerapp.constants.Sex;

import java.time.LocalDate;

/**
 * An entity that represents a user's account information, personal details,
 * and relevant biological characteristics.
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
     * Internal name. For authentication.
     */
    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @Column(nullable = false, length = 40)
    private String password;

    /**
     * Preferred name. For addressing the user.
     */
    @Column(nullable = false, length = 25)
    private String nickname;

    /**
     * Gender identity. For addressing the user.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    /**
     * Biological sex. For estimating energy expenditure.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    /**
     * Height expressed in centimeters.
     */
    @Column(nullable = false)
    private double height;

    public int deriveAgeInYears() {
        return birthDate.until(LocalDate.now()).getYears();
    }
}