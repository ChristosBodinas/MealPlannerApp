package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Day {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false) private double calorieGoal;
    @Column(nullable = false) private double proteinGoal;
    @Column(nullable = false) private double carbsGoal;
    @Column(nullable = false) private double fatGoal;
    @Column(nullable = false) private double fiberGoal;
}
