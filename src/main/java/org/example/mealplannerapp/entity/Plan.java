package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor
public class Plan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double startWeight;

    @Column(nullable = false)
    private double averageDeficit;

    @ElementCollection
    @CollectionTable(name = "categories", joinColumns = @JoinColumn(name = "plan_id"))
    private List<String> categories;

    @OneToMany(mappedBy = "plan")
    private Set<Day> days;
}
