package org.example.mealplannerapp.entity.entries;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.entity.Day;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public class Entry {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @ManyToOne @JoinColumn(name = "day_id")
    private Day day;

    // TO DO: Figure out how to handle categories!
    @Column(nullable = false)
    private int category;

    @Column(nullable = false)
    private int position;

    // These values are calculated and stored here as a snapshot.
    // This ensures faster access for aggregate methods, and prevents unexpected changes
    // if the underlying food changes.
    @Column(nullable = false) private double calories;
    @Column(nullable = false) private double protein;
    @Column(nullable = false) private double carbs;
    @Column(nullable = false) private double fat;
    @Column(nullable = false) private double fiber;
    @Column(nullable = false) private double price;
}
