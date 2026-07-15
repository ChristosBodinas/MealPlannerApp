package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.entity.entry.Entry;

import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
public class Day {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne @JoinColumn(name = "plan_id")
    private Plan plan;

    @OneToMany(mappedBy="day")
    private Set<Entry> entries;

    @Column(nullable = false) private double caloriesGoal;
    @Column(nullable = false) private double proteinGoal;
    @Column(nullable = false) private double carbsGoal;
    @Column(nullable = false) private double fatGoal;
    @Column(nullable = false) private double fiberGoal;
}