package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.constants.Gender;
import org.example.mealplannerapp.constants.Sex;
import org.hibernate.annotations.Temporal;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @Column(nullable = false, length = 40)
    private String password;

    // These define how the user should be addressed.
    @Column(nullable = false, length = 25)
    private String nickname;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private Gender gender;

    // Information used to calculate nutrition goals.
    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(nullable = false)
    private double height;
}
