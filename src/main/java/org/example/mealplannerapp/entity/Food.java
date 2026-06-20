package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Food {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @Column(nullable = false, length = 45)
    private String name;

    // Left this nullable to allow for generic foods.
    @Column (length = 45) private String brand;

    // Nutritional values and prices are expressed per 100 grams of the given food.
    @Column(nullable = false) private double calories100g;
    @Column(nullable = false) private double protein100g;
    @Column(nullable = false) private double carbs100g;
    @Column(nullable = false) private double fat100g;
    @Column(nullable = false) private double fiber100g;
    @Column(nullable = false) private double price100g;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;
}
