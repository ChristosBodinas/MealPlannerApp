package org.example.mealplannerapp.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;


/**
 * An entity that represents a meal plan and its overall parameters.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * User who owns the plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Days that comprise the plan.
     */
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("position ASC")    // TODO: Possibly unnecessary/ineffectual when using sets.
    private Set<Day> days;
    
}
