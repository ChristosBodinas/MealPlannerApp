package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;

import org.example.mealplannerapp.dto.day.DayGoalsResponse;
import org.example.mealplannerapp.entity.entry.Entry;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    // WE PROBABLY DON'T NEED THIS TO BE BIDIRECTIONAL
    @OneToMany(mappedBy = "day")
    private Set<Entry> entries;

    @Column(nullable = false)
    private double caloriesGoal;
    @Column(nullable = false)
    private double proteinGoal;
    @Column(nullable = false)
    private double carbsGoal;
    @Column(nullable = false)
    private double fatGoal;
    @Column(nullable = false)
    private double fiberGoal;

}